// com/cuisinvoisin/interfaces/rest/AddressController.java
package com.cuisinvoisin.interfaces.rest;

import com.cuisinvoisin.application.bean.request.SaveAddressRequest;
import com.cuisinvoisin.application.bean.response.AddressResponse;
import com.cuisinvoisin.domain.services.AddressService;
import com.cuisinvoisin.shared.util.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.ADDRESSES_BASE)
@RequiredArgsConstructor
@Tag(name = "Addresses", description = "Saved delivery address management")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @Operation(summary = "List saved addresses for the authenticated client")
    public ResponseEntity<List<AddressResponse>> getAddresses(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(addressService.getAddresses(UUID.fromString(principal.getUsername())));
    }

    @PostMapping
    @Operation(summary = "Save a new delivery address")
    public ResponseEntity<AddressResponse> saveAddress(@AuthenticationPrincipal UserDetails principal,
                                                        @Valid @RequestBody SaveAddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addressService.saveAddress(UUID.fromString(principal.getUsername()), request));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a saved address")
    public ResponseEntity<AddressResponse> updateAddress(@AuthenticationPrincipal UserDetails principal,
                                                          @PathVariable UUID id,
                                                          @Valid @RequestBody SaveAddressRequest request) {
        return ResponseEntity.ok(addressService.updateAddress(
                UUID.fromString(principal.getUsername()), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a saved address")
    public ResponseEntity<Void> deleteAddress(@AuthenticationPrincipal UserDetails principal,
                                               @PathVariable UUID id) {
        addressService.deleteAddress(UUID.fromString(principal.getUsername()), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/default")
    @Operation(summary = "Set an address as the default delivery address")
    public ResponseEntity<Void> setDefault(@AuthenticationPrincipal UserDetails principal,
                                            @PathVariable UUID id) {
        addressService.setDefault(UUID.fromString(principal.getUsername()), id);
        return ResponseEntity.noContent().build();
    }
}
