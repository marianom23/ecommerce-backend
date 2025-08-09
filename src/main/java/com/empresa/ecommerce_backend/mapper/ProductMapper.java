package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.dto.request.ProductRequest;
import com.empresa.ecommerce_backend.dto.response.ProductResponse;
import com.empresa.ecommerce_backend.model.*;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "stock", ignore = true)
    @Mapping(target = "stockTrackingMode", ignore = true)
    @Mapping(target = "brand", source = "brandId", qualifiedByName = "brandFromId")
    @Mapping(target = "category", source = "categoryId", qualifiedByName = "categoryFromId")
    Product toEntity(ProductRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "stock", ignore = true)
    @Mapping(target = "stockTrackingMode", ignore = true)
    @Mapping(target = "brand", source = "brandId", qualifiedByName = "brandFromId")
    @Mapping(target = "category", source = "categoryId", qualifiedByName = "categoryFromId")
    void updateEntity(@MappingTarget Product entity, ProductRequest dto);

    @Mapping(target = "title", source = "name")
    @Mapping(target = "reviews", expression = "java(0)")
    @Mapping(target = "discountedPrice", expression = "java(computeDiscountedPrice(product))")
    @Mapping(target = "imgs", expression = "java(buildImages(product))")
    ProductResponse toResponse(Product product);

    List<ProductResponse> toResponseList(List<Product> products);

    @Named("brandFromId")
    default Brand brandFromId(Long id) { if (id == null) return null; Brand b = new Brand(); b.setId(id); return b; }

    @Named("categoryFromId")
    default Category categoryFromId(Long id) { if (id == null) return null; Category c = new Category(); c.setId(id); return c; }

    default ProductResponse.ImagesDto buildImages(Product p) {
        List<String> all = p.getImages().stream()
                .filter(pi -> pi.getVariant() == null)
                .sorted(Comparator.comparing(ProductImage::getPosition, Comparator.nullsLast(Integer::compareTo)))
                .map(ProductImage::getUrl)
                .toList();

        ProductResponse.ImagesDto imgs = new ProductResponse.ImagesDto();
        imgs.setThumbnails(all.stream().limit(4).toList());
        imgs.setPreviews(all);
        return imgs;
    }

    default BigDecimal computeDiscountedPrice(Product p) {
        if (p == null || p.getPrice() == null) return null;
        BigDecimal base = p.getPrice();
        if (p.getDiscounts() == null || p.getDiscounts().isEmpty()) return base;

        BigDecimal best = base;
        for (Discount d : p.getDiscounts()) {
            BigDecimal pct = d.getPercentage(); // ajustá al nombre real
            if (pct != null && pct.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal off = base.multiply(pct).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                BigDecimal candidate = base.subtract(off);
                if (candidate.compareTo(best) < 0) best = candidate;
            }
        }
        return best.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }
}
