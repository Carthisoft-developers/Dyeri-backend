// com/cuisinvoisin/application/mappers/ReviewMapper.java
package com.cuisinvoisin.application.mappers;

import com.cuisinvoisin.application.bean.response.ReviewResponse;
import com.cuisinvoisin.domain.entities.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {
    @Mapping(target = "authorName", source = "author.name")
    @Mapping(target = "authorAvatar", ignore = true)
    ReviewResponse toResponse(Review review);
}
