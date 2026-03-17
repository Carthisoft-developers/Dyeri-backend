// com/cuisinvoisin/application/mappers/NotificationMapper.java
package com.cuisinvoisin.application.mappers;

import com.cuisinvoisin.application.bean.response.NotificationResponse;
import com.cuisinvoisin.domain.entities.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {
    NotificationResponse toResponse(Notification notification);
}
