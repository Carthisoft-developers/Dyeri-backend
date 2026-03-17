// com/cuisinvoisin/application/services/AddressServiceImpl.java
package com.cuisinvoisin.application.services;

import com.cuisinvoisin.application.bean.request.SaveAddressRequest;
import com.cuisinvoisin.application.bean.response.AddressResponse;
import com.cuisinvoisin.application.mappers.AddressMapper;
import com.cuisinvoisin.domain.entities.Client;
import com.cuisinvoisin.domain.entities.SavedAddress;
import com.cuisinvoisin.domain.exceptions.ResourceNotFoundException;
import com.cuisinvoisin.domain.exceptions.UnauthorizedException;
import com.cuisinvoisin.domain.repositories.ClientRepository;
import com.cuisinvoisin.domain.repositories.SavedAddressRepository;
import com.cuisinvoisin.domain.services.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final SavedAddressRepository addressRepository;
    private final ClientRepository clientRepository;
    private final AddressMapper addressMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getAddresses(UUID clientId) {
        return addressRepository.findByClient_IdOrderByIsDefaultDescCreatedAtDesc(clientId)
                .stream().map(addressMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public AddressResponse saveAddress(UUID clientId, SaveAddressRequest request) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));

        if (request.isDefault()) {
            clearDefaultFlags(clientId);
        }

        SavedAddress address = SavedAddress.builder()
                .client(client)
                .label(request.label())
                .address(request.address())
                .additionalInfo(request.additionalInfo())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .isDefault(request.isDefault())
                .build();

        return addressMapper.toResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(UUID clientId, UUID addressId, SaveAddressRequest request) {
        SavedAddress address = findOwnedAddress(clientId, addressId);

        address.setLabel(request.label());
        address.setAddress(request.address());
        address.setAdditionalInfo(request.additionalInfo());
        address.setLatitude(request.latitude());
        address.setLongitude(request.longitude());

        if (request.isDefault() && !address.isDefault()) {
            clearDefaultFlags(clientId);
            address.setDefault(true);
        }

        return addressMapper.toResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public void deleteAddress(UUID clientId, UUID addressId) {
        SavedAddress address = findOwnedAddress(clientId, addressId);
        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public void setDefault(UUID clientId, UUID addressId) {
        findOwnedAddress(clientId, addressId); // ownership check
        clearDefaultFlags(clientId);
        SavedAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));
        address.setDefault(true);
        addressRepository.save(address);
    }

    private SavedAddress findOwnedAddress(UUID clientId, UUID addressId) {
        SavedAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));
        if (!address.getClient().getId().equals(clientId)) {
            throw new UnauthorizedException("Address does not belong to this client");
        }
        return address;
    }

    private void clearDefaultFlags(UUID clientId) {
        addressRepository.findByClient_IdOrderByIsDefaultDescCreatedAtDesc(clientId)
                .forEach(a -> { a.setDefault(false); addressRepository.save(a); });
    }
}
