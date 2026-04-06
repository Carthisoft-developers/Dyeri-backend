package com.dyeri.core.domain.services;
import com.dyeri.core.application.bean.request.SaveAddressRequest;
import com.dyeri.core.application.bean.response.AddressResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

/** Inbound port for saved address management. */
public interface AddressService {
    Flux<AddressResponse> getAddresses(UUID clientId);
    Mono<AddressResponse> saveAddress(UUID clientId, SaveAddressRequest request);
    Mono<AddressResponse> updateAddress(UUID clientId, UUID addressId, SaveAddressRequest request);
    Mono<Void> deleteAddress(UUID clientId, UUID addressId);
    Mono<Void> setDefault(UUID clientId, UUID addressId);
}
