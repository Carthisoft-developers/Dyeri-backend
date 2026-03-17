// com/cuisinvoisin/application/mappers/DishMapper.java
package com.cuisinvoisin.application.mappers;

import com.cuisinvoisin.application.bean.response.DishResponse;
import com.cuisinvoisin.application.bean.response.DishSummaryResponse;
import com.cuisinvoisin.domain.entities.Dish;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {CookMapper.class})
public interface DishMapper {
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "cook", source = "cook")
    DishResponse toResponse(Dish dish);

    @Mapping(target = "cook", source = "cook")
    DishSummaryResponse toSummary(Dish dish);
}
