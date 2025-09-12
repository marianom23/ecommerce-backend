// src/main/java/com/empresa/ecommerce_backend/mapper/BillingProfileMapper.java
package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.dto.request.BillingProfileRequest;
import com.empresa.ecommerce_backend.dto.response.BillingProfileResponse;
import com.empresa.ecommerce_backend.model.BillingProfile;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BillingProfileMapper {

    // DTO -> Entity (el service setea user y billingAddress)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "billingAddress", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "isDefault", target = "defaultProfile")
    BillingProfile toEntity(BillingProfileRequest dto);

    // Entity -> Response
    @Mapping(target = "billingAddressId", source = "billingAddress.id")
    BillingProfileResponse toResponse(BillingProfile entity);

    // Update parcial (PUT/PATCH)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "billingAddress", ignore = true) // se maneja en el service si viene otro ID
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "isDefault", target = "defaultProfile")
    void updateFromDto(BillingProfileRequest dto, @MappingTarget BillingProfile entity);
}
