// com/cuisinvoisin/application/mappers/AddressMapper.java
package com.cuisinvoisin.application.mappers;

import com.cuisinvoisin.application.bean.response.AddressResponse;
import com.cuisinvoisin.domain.entities.SavedAddress;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AddressMapper {
    AddressResponse toResponse(SavedAddress address);
}
