package com.dyeri.core.application.bean.response;
import java.util.List;
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {}
