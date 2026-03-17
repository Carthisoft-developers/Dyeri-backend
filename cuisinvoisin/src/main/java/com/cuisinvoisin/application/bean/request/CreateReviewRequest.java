// com/cuisinvoisin/application/bean/request/CreateReviewRequest.java
package com.cuisinvoisin.application.bean.request;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record CreateReviewRequest(
        @NotNull UUID orderId,
        UUID dishId,
        @Min(1) @Max(5) int rating,
        @Size(max = 500) String text
) {}
