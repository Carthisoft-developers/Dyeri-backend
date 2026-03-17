// com/cuisinvoisin/application/bean/response/AddressResponse.java
package com.cuisinvoisin.application.bean.response;

import java.util.UUID;

public record AddressResponse(UUID id, String label, String address, String additionalInfo,
        double latitude, double longitude, boolean isDefault) {}
