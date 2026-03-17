// com/cuisinvoisin/application/mappers/CartMapper.java
package com.cuisinvoisin.application.mappers;

import com.cuisinvoisin.application.bean.response.CartItemResponse;
import com.cuisinvoisin.application.bean.response.CartResponse;
import com.cuisinvoisin.domain.entities.Cart;
import com.cuisinvoisin.domain.entities.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {DishMapper.class})
public interface CartMapper {
    CartResponse toResponse(Cart cart);

    @Mapping(target = "lineTotal", expression = "java(item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))")
    @Mapping(target = "dish", source = "dish")
    CartItemResponse toItemResponse(CartItem item);
}
