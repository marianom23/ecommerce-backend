package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.dto.request.*;
import com.empresa.ecommerce_backend.dto.response.*;
import com.empresa.ecommerce_backend.model.*;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = PurchaseLotMapper.class)
public interface PurchaseOrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "purchaseDate", expression = "java(java.time.LocalDate.now())")
    @Mapping(target = "lots", source = "lots")
    PurchaseOrder toEntity(PurchaseOrderRequest dto);

    PurchaseOrderResponse toResponse(PurchaseOrder entity);
}
