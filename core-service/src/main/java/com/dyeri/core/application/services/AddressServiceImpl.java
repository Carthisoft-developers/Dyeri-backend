// com/dyeri/core/application/services/AddressServiceImpl.java
package com.dyeri.core.application.services;

import com.dyeri.core.application.bean.request.SaveAddressRequest;
import com.dyeri.core.application.bean.response.AddressResponse;
import com.dyeri.core.domain.entities.SavedAddress;
import com.dyeri.core.domain.exceptions.*;
import com.dyeri.core.domain.repositories.SavedAddressRepository;
import com.dyeri.core.domain.services.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final SavedAddressRepository addressRepository;
    private final TransactionalOperator txOperator;

    @Override
    public Flux<AddressResponse> getAddresses(UUID clientId) {
        return addressRepository.findByClientIdOrderByDefaultAddressDescCreatedAtDesc(clientId)
                .map(this::toResponse);
    }

    @Override
    public Mono<AddressResponse> saveAddress(UUID clientId, SaveAddressRequest request) {
        Mono<Void> clearDefault = request.isDefault()
                ? addressRepository.clearDefaultsByClientId(clientId) : Mono.empty();
        return clearDefault.then(addressRepository.save(SavedAddress.builder()
                        .id(UUID.randomUUID()).clientId(clientId)
                        .label(request.label()).address(request.address())
                        .additionalInfo(request.additionalInfo())
                        .latitude(request.latitude()).longitude(request.longitude())
                        .defaultAddress(request.isDefault()).build()))
                .map(this::toResponse)
                .as(txOperator::transactional);
    }

    @Override
    public Mono<AddressResponse> updateAddress(UUID clientId, UUID addressId, SaveAddressRequest request) {
        return addressRepository.findById(addressId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Address", addressId)))
                .flatMap(addr -> {
                    if (!addr.getClientId().equals(clientId))
                        return Mono.error(new UnauthorizedException("Not your address"));
                    addr.setLabel(request.label()); addr.setAddress(request.address());
                    addr.setAdditionalInfo(request.additionalInfo());
                    addr.setLatitude(request.latitude()); addr.setLongitude(request.longitude());
                    return addressRepository.save(addr);
                })
                .map(this::toResponse);
    }

    @Override
    public Mono<Void> deleteAddress(UUID clientId, UUID addressId) {
        return addressRepository.findById(addressId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Address", addressId)))
                .flatMap(addr -> {
                    if (!addr.getClientId().equals(clientId))
                        return Mono.error(new UnauthorizedException("Not your address"));
                    return addressRepository.delete(addr);
                });
    }

    @Override
    public Mono<Void> setDefault(UUID clientId, UUID addressId) {
        return addressRepository.clearDefaultsByClientId(clientId)
                .then(addressRepository.findById(addressId))
                .flatMap(addr -> { addr.setDefaultAddress(true); return addressRepository.save(addr); })
                .then()
                .as(txOperator::transactional);
    }

    private AddressResponse toResponse(SavedAddress a) {
        return new AddressResponse(a.getId(), a.getLabel(), a.getAddress(), a.getAdditionalInfo(),
                a.getLatitude() != null ? a.getLatitude() : 0,
                a.getLongitude() != null ? a.getLongitude() : 0,
                Boolean.TRUE.equals(a.getDefaultAddress()));
    }
}
