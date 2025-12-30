package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.dto.request.ProductRequest;
import com.empresa.ecommerce_backend.dto.response.ProductResponse;
import com.empresa.ecommerce_backend.enums.FulfillmentType;
import com.empresa.ecommerce_backend.model.*;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "variants", ignore = true) // las variantes se gestionan aparte
    @Mapping(target = "brand", source = "brandId", qualifiedByName = "brandFromId")
    @Mapping(target = "category", source = "categoryId", qualifiedByName = "categoryFromId")
    Product toEntity(ProductRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "brand", source = "brandId", qualifiedByName = "brandFromId")
    @Mapping(target = "category", source = "categoryId", qualifiedByName = "categoryFromId")
    void updateEntity(@MappingTarget Product entity, ProductRequest dto);

    @Mapping(target = "title", source = "name")
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "totalReviews", ignore = true)
    @Mapping(target = "price", expression = "java(computeRepresentativePrice(product))")
    @Mapping(target = "discountedPrice", expression = "java(computeDiscountedPrice(product))")
    @Mapping(target = "priceWithTransfer", expression = "java(computePriceWithTransfer(product))") // 👈 NUEVO
    @Mapping(target = "imgs", expression = "java(buildImages(product))")
    @Mapping(target = "variantCount", expression = "java(product.getVariants() != null ? product.getVariants().size() : 0)")
    @Mapping(target = "defaultVariantId", expression = "java(getDefaultVariantId(product))")
    @Mapping(target = "fulfillmentType", expression = "java(resolveFulfillmentType(product))") // 👈 NUEVO
    ProductResponse toResponse(Product product);

    List<ProductResponse> toResponseList(List<Product> products);

    @Named("brandFromId")
    default Brand brandFromId(Long id) {
        if (id == null) return null;
        Brand b = new Brand();
        b.setId(id);
        return b;
    }

    @Named("categoryFromId")
    default Category categoryFromId(Long id) {
        if (id == null) return null;
        Category c = new Category();
        c.setId(id);
        return c;
    }

    // ---------- Helpers ----------

    default Long getDefaultVariantId(Product p) {
        if (p.getVariants() != null && p.getVariants().size() == 1) {
            return p.getVariants().iterator().next().getId();
        }
        return null;
    }

    /** Precio representativo: el más barato de sus variantes */
    default BigDecimal computeRepresentativePrice(Product p) {
        if (p.getVariants() == null || p.getVariants().isEmpty()) return null;
        return p.getVariants().stream()
                .map(ProductVariant::getPrice)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(null);
    }

    /** Calcula precio con descuento (si existen descuentos) */
    default BigDecimal computeDiscountedPrice(Product p) {
        BigDecimal base = computeRepresentativePrice(p);
        if (base == null) return null;

        if (p.getDiscounts() == null || p.getDiscounts().isEmpty()) return base;

        BigDecimal best = base;
        for (Discount d : p.getDiscounts()) {
            BigDecimal pct = d.getPercentage();
            if (pct != null && pct.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal off = base.multiply(pct)
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                BigDecimal candidate = base.subtract(off);
                if (candidate.compareTo(best) < 0) best = candidate;
            }
        }
        return best.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    /** Calcula precio con transferencia (10% off sobre el precio con descuento) */
    default BigDecimal computePriceWithTransfer(Product p) {
        BigDecimal discounted = computeDiscountedPrice(p);
        if (discounted == null) return null;
        // 10% de descuento adicional
        BigDecimal transferDiscount = discounted.multiply(new BigDecimal("0.10"));
        return discounted.subtract(transferDiscount).setScale(2, RoundingMode.HALF_UP);
    }

    /** Construye DTO de imágenes (solo imágenes del producto, no de variantes) */
    default ProductResponse.ImagesDto buildImages(Product p) {
        List<String> all = p.getImages().stream()
                .sorted(Comparator.comparing((ProductImage img) -> img.getVariant() != null) // false (null) va primero
                        .thenComparing(ProductImage::getPosition, Comparator.nullsLast(Integer::compareTo)))
                .map(ProductImage::getUrl)
                .toList();

        ProductResponse.ImagesDto imgs = new ProductResponse.ImagesDto();
        imgs.setUrls(all);
        return imgs;
    }

    /** Tipo de fulfillment representativo del producto:
     *  - Única variante → su tipo.
     *  - Varias variantes:
     *      * si todas comparten el mismo tipo → ese tipo
     *      * si hay mezcla → "MIXED"
     */
    default String resolveFulfillmentType(Product p) {
        if (p.getVariants() == null || p.getVariants().isEmpty()) {
            return "PHYSICAL"; // por defecto, para no romper el front
        }
        if (p.getVariants().size() == 1) {
            ProductVariant v = p.getVariants().iterator().next();
            FulfillmentType ft = v.getFulfillmentType();
            return ft != null ? ft.name() : "PHYSICAL";
        }
        Set<String> types = p.getVariants().stream()
                .map(ProductVariant::getFulfillmentType)
                .map(ft -> ft != null ? ft.name() : "PHYSICAL")
                .collect(Collectors.toSet());
        return (types.size() == 1) ? types.iterator().next() : "MIXED";
    }
}
