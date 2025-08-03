package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.dto.request.ProductRequest;
import com.empresa.ecommerce_backend.dto.response.ProductResponse;
import com.empresa.ecommerce_backend.model.Brand;
import com.empresa.ecommerce_backend.model.Category;
import com.empresa.ecommerce_backend.model.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    // CREATE: ignoramos id, stock y stockTrackingMode (lo setea la lógica de negocio)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "stock", ignore = true)
    @Mapping(target = "stockTrackingMode", ignore = true)
    @Mapping(target = "brand", source = "brandId", qualifiedByName = "brandFromId")
    @Mapping(target = "category", source = "categoryId", qualifiedByName = "categoryFromId")
    Product toEntity(ProductRequest dto);

    // UPDATE: no tocar stock ni stockTrackingMode
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "stock", ignore = true)
    @Mapping(target = "stockTrackingMode", ignore = true)
    @Mapping(target = "brand", source = "brandId", qualifiedByName = "brandFromId")
    @Mapping(target = "category", source = "categoryId", qualifiedByName = "categoryFromId")
    void updateEntity(@MappingTarget Product entity, ProductRequest dto);

    // RESPONSE: podés exponer brandName, categoryName y (opcional) stockTrackingMode
    @Mapping(source = "brand.name",    target = "brandName")
    @Mapping(source = "category.name", target = "categoryName")
    // Si ProductResponse tiene un campo 'stockTrackingMode' (String o Enum), MapStruct lo mapea solo.
    ProductResponse toResponse(Product product);

    @Named("brandFromId")
    default Brand brandFromId(Long id) {
        if (id == null) return null;
        Brand b = new Brand(); b.setId(id); return b;
    }

    @Named("categoryFromId")
    default Category categoryFromId(Long id) {
        if (id == null) return null;
        Category c = new Category(); c.setId(id); return c;
    }
}
