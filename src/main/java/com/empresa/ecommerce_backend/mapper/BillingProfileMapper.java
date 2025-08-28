// src/main/java/com/empresa/ecommerce_backend/mapper/BillingProfileMapper.java
package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.dto.request.BillingProfileRequest;
import com.empresa.ecommerce_backend.dto.response.BillingProfileResponse;
import com.empresa.ecommerce_backend.model.Address;
import com.empresa.ecommerce_backend.model.BillingProfile;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BillingProfileMapper {

    // create: resuelve Address en el service; acá ignoramos y luego seteamos
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "billingAddress", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    // puente DTO -> entidad: isDefault -> defaultProfile
    @Mapping(source = "isDefault", target = "defaultProfile")
    BillingProfile toEntity(BillingProfileRequest dto);

    // entity -> response
    @Mapping(target = "billingAddressId", source = "billingAddress.id")
    BillingProfileResponse toResponse(BillingProfile entity);

    // update parcial (PUT)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "billingAddress", ignore = true) // se maneja en service si viene otro ID
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "isDefault", target = "defaultProfile")
    void updateFromDto(BillingProfileRequest dto, @MappingTarget BillingProfile entity);

    // helper (opcional): para setear la address si la resolviste en el service
    default void setAddress(BillingProfile bp, Address addr) {
        bp.setBillingAddress(addr);
    }
}
