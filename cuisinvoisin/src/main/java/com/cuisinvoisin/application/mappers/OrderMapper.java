// com/cuisinvoisin/application/mappers/OrderMapper.java
package com.cuisinvoisin.application.mappers;

import com.cuisinvoisin.application.bean.response.OrderItemResponse;
import com.cuisinvoisin.application.bean.response.OrderResponse;
import com.cuisinvoisin.application.bean.response.OrderSummaryResponse;
import com.cuisinvoisin.application.bean.response.TimelineStepResponse;
import com.cuisinvoisin.domain.entities.Order;
import com.cuisinvoisin.domain.entities.OrderItem;
import com.cuisinvoisin.domain.entities.TimelineStep;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {CookMapper.class})
public interface OrderMapper {
    @Mapping(target = "summary", source = ".")
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "timeline", ignore = true)
    @Mapping(target = "cook", source = "cook")
    OrderResponse toResponse(Order order);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "mode", source = "mode")
    @Mapping(target = "total", source = "total")
    @Mapping(target = "deliveryAddress", source = "deliveryAddress")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "eta", source = "eta")
    OrderSummaryResponse toSummary(Order order);

    OrderItemResponse toItemResponse(OrderItem item);

    TimelineStepResponse toStepResponse(TimelineStep step);
}
