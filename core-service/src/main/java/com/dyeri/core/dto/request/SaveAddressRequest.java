package com.dyeri.core.application.bean.request;
import jakarta.validation.constraints.NotBlank;
public record SaveAddressRequest(
        @NotBlank String label, @NotBlank String address, String additionalInfo,
        double latitude, double longitude, boolean isDefault
) {}
