package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.dto.request.*;
import com.empresa.ecommerce_backend.dto.response.*;
import com.empresa.ecommerce_backend.model.*;
import org.mapstruct.*;

import java.util.List;


@Mapper(componentModel = "spring")
public interface PurchaseLotMapper {

    @Mapping(target = "product.id", source = "productId")
    PurchaseLot toEntity(PurchaseLotRequest dto);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    PurchaseLotResponse toResponse(PurchaseLot lot);

    List<PurchaseLot> toEntityList(List<PurchaseLotRequest> lotDtos);

    List<PurchaseLotResponse> toResponseList(List<PurchaseLot> lots);
}
