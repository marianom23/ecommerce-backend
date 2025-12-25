package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.dto.request.SupplierRequest;
import com.empresa.ecommerce_backend.dto.response.SupplierResponse;
import com.empresa.ecommerce_backend.model.Supplier;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    Supplier toEntity(SupplierRequest request);

    SupplierResponse toResponse(Supplier entity);

    List<SupplierResponse> toResponseList(List<Supplier> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(SupplierRequest request, @MappingTarget Supplier entity);
}
