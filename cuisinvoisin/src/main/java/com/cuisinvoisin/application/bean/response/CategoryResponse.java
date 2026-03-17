// com/cuisinvoisin/application/bean/response/CategoryResponse.java
package com.cuisinvoisin.application.bean.response;

import java.util.UUID;

public record CategoryResponse(UUID id, String name, String icon, String image) {}
