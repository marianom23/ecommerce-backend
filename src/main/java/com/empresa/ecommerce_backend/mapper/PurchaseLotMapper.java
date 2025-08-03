package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.dto.request.PurchaseLotRequest;
import com.empresa.ecommerce_backend.dto.response.PurchaseLotResponse;
import com.empresa.ecommerce_backend.model.Product;
import com.empresa.ecommerce_backend.model.ProductVariant;
import com.empresa.ecommerce_backend.model.PurchaseLot;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PurchaseLotMapper {

    @Mapping(target = "product",        source = "productId",        qualifiedByName = "idToProduct")
    @Mapping(target = "productVariant", source = "productVariantId", qualifiedByName = "idToVariant")
    @Mapping(target = "purchaseOrder", ignore = true)
    @Mapping(target = "id", ignore = true)
    PurchaseLot toEntity(PurchaseLotRequest dto);

    @Mapping(source = "product.id",        target = "productId")
    @Mapping(source = "product.name",      target = "productName")
    @Mapping(source = "productVariant.id", target = "productVariantId")
    PurchaseLotResponse toResponse(PurchaseLot lot);

    List<PurchaseLot> toEntityList(List<PurchaseLotRequest> lotDtos);
    List<PurchaseLotResponse> toResponseList(List<PurchaseLot> lots);

    @Named("idToProduct")
    default Product idToProduct(Long id) {
        if (id == null) return null;
        Product p = new Product();
        p.setId(id);
        return p;
    }

    @Named("idToVariant")
    default ProductVariant idToVariant(Long id) {
        if (id == null) return null;
        ProductVariant v = new ProductVariant();
        v.setId(id);
        return v;
    }
}
