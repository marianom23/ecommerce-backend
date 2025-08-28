// src/main/java/com/empresa/ecommerce_backend/mapper/AddressMapper.java
package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.dto.request.AddressRequest;
import com.empresa.ecommerce_backend.dto.response.AddressResponse;
import com.empresa.ecommerce_backend.model.Address;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AddressMapper {

    // Request -> Entity (para crear)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)            // se setea en el service
    @Mapping(target = "createdAt", ignore = true)       // @PrePersist lo setea
    @Mapping(target = "updatedAt", ignore = true)       // @PreUpdate lo setea
    @Mapping(target = "lastUsedAt", ignore = true)      // lo setea el service
    Address toEntity(AddressRequest dto);

    // Entity -> Response
    AddressResponse toResponse(Address entity);

    // Update in-place (PUT): solo campos editables
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastUsedAt", ignore = true)
    void updateFromDto(AddressRequest dto, @MappingTarget Address entity);
}
