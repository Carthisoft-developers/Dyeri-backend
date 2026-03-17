// com/cuisinvoisin/domain/services/AddressService.java
package com.cuisinvoisin.domain.services;

import com.cuisinvoisin.application.bean.request.SaveAddressRequest;
import com.cuisinvoisin.application.bean.response.AddressResponse;

import java.util.List;
import java.util.UUID;

/**
 * Inbound port for client saved-address management.
 */
public interface AddressService {
    /** Return all saved addresses for the client, default first. */
    List<AddressResponse> getAddresses(UUID clientId);
    /** Persist a new address; sets it as default if requested. */
    AddressResponse saveAddress(UUID clientId, SaveAddressRequest request);
    /** Update label/coordinates of an existing address. */
    AddressResponse updateAddress(UUID clientId, UUID addressId, SaveAddressRequest request);
    /** Delete an address by id. */
    void deleteAddress(UUID clientId, UUID addressId);
    /** Make an address the default, clearing any previous default. */
    void setDefault(UUID clientId, UUID addressId);
}
