// com/cuisinvoisin/application/bean/response/PageResponse.java
package com.cuisinvoisin.application.bean.response;

import java.util.List;

public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {}
