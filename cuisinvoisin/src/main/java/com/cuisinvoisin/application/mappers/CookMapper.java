// com/cuisinvoisin/application/mappers/CookMapper.java
package com.cuisinvoisin.application.mappers;

import com.cuisinvoisin.application.bean.response.CookResponse;
import com.cuisinvoisin.application.bean.response.CookSummaryResponse;
import com.cuisinvoisin.domain.entities.Cook;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CookMapper {
    CookResponse toResponse(Cook cook);

    @Mapping(target = "distanceKm", ignore = true)
    CookSummaryResponse toSummary(Cook cook);

    @Mapping(target = "distanceKm", ignore = true)
    CookSummaryResponse toSummaryWithDistance(Cook cook);
}
