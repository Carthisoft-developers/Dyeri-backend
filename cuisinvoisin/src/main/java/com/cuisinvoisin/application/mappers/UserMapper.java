// com/cuisinvoisin/application/mappers/UserMapper.java
package com.cuisinvoisin.application.mappers;

import com.cuisinvoisin.application.bean.response.UserResponse;
import com.cuisinvoisin.domain.entities.Cook;
import com.cuisinvoisin.domain.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    @Mapping(target = "avatar", source = "avatar")
    UserResponse toResponse(User user);

    @Mapping(target = "avatar", source = "avatar")
    UserResponse cookToResponse(Cook cook);
}
