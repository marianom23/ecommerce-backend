package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.dto.request.ProductVariantRequest;
import com.empresa.ecommerce_backend.dto.response.ProductVariantResponse;
import com.empresa.ecommerce_backend.model.Product;
import com.empresa.ecommerce_backend.model.ProductVariant;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductVariantMapper {

    // Crear variante: NO mapear stock ni id. El 'product' se setea en el servicio.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "stock", ignore = true)
    @Mapping(target = "product", ignore = true) // lo asignás en el servicio
    ProductVariant toEntity(ProductVariantRequest req);

    // Update parcial: NO tocar stock
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "stock", ignore = true)
    void updateEntityFromRequest(ProductVariantRequest req, @MappingTarget ProductVariant entity);

    // Respuesta
    @Mapping(target = "productId", source = "product.id")
    ProductVariantResponse toResponse(ProductVariant entity);

    // (Opcional) Si alguna vez agregás productId al request, podés usar este helper:
    @Named("productFromId")
    default Product productFromId(Long id) {
        if (id == null) return null;
        Product p = new Product();
        p.setId(id);
        return p;
    }
}
