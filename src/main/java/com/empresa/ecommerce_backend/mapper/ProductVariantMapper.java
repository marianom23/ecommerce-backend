// src/main/java/com/empresa/ecommerce_backend/mapper/ProductVariantMapper.java
package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.dto.request.ProductVariantRequest;
import com.empresa.ecommerce_backend.dto.response.ProductImageResponse;
import com.empresa.ecommerce_backend.dto.response.ProductVariantResponse;
import com.empresa.ecommerce_backend.model.Product;
import com.empresa.ecommerce_backend.model.ProductVariant;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductVariantMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true) // lo setea el service
    ProductVariant toEntity(ProductVariantRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(ProductVariantRequest req, @MappingTarget ProductVariant entity);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "images", expression = "java(mapImages(entity))")
    ProductVariantResponse toResponse(ProductVariant entity);

    default List<ProductImageResponse> mapImages(ProductVariant variant) {
        return variant.getProduct().getImages().stream()
                .filter(img -> img.getVariant() != null && img.getVariant().getId().equals(variant.getId()))
                .map(img -> new ProductImageResponse(
                        img.getId(),
                        img.getUrl(),
                        img.getPosition(),
                        variant.getId()
                ))
                .collect(Collectors.toList());
    }

    @Named("productFromId")
    default Product productFromId(Long id) {
        if (id == null) return null;
        Product p = new Product();
        p.setId(id);
        return p;
    }
}
