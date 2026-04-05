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
    @Mapping(target = "console", source = "consoleId", qualifiedByName = "consoleFromId")
    @Mapping(target = "parentGame", source = "parentGameId", qualifiedByName = "productFromId")
    @Mapping(target = "dlcs", ignore = true)
    Product toEntity(ProductRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "dlcs", ignore = true)
    @Mapping(target = "brand", source = "brandId", qualifiedByName = "brandFromId")
    @Mapping(target = "category", source = "categoryId", qualifiedByName = "categoryFromId")
    @Mapping(target = "console", source = "consoleId", qualifiedByName = "consoleFromId")
    @Mapping(target = "parentGame", source = "parentGameId", qualifiedByName = "productFromId")
    void updateEntity(@MappingTarget Product entity, ProductRequest dto);

    @Mapping(target = "title", source = "product.name")
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "totalReviews", ignore = true)
    @Mapping(target = "price", expression = "java(computeRepresentativePrice(product))")
    @Mapping(target = "discountedPrice", expression = "java(computeDiscountedPrice(product, globalDiscounts))")
    @Mapping(target = "priceWithTransfer", expression = "java(computePriceWithTransfer(product, globalDiscounts, transferDiscountPct))")
    @Mapping(target = "imgs", expression = "java(buildImages(product))")
    @Mapping(target = "variantCount", expression = "java(product.getVariants() != null ? product.getVariants().size() : 0)")
    @Mapping(target = "defaultVariantId", expression = "java(getDefaultVariantId(product))")
    @Mapping(target = "fulfillmentType", expression = "java(resolveFulfillmentType(product))")
    @Mapping(target = "consoleName", source = "console.name")
    ProductResponse toResponse(Product product, @Context List<Discount> globalDiscounts, @Context BigDecimal transferDiscountPct);

    /** Overload for simple mapping without context */
    default ProductResponse toResponse(Product product) {
        return toResponse(product, null, null);
    }

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

    @Named("consoleFromId")
    default Console consoleFromId(Long id) {
        if (id == null) return null;
        Console c = new Console();
        c.setId(id);
        return c;
    }

    @Named("productFromId")
    default Product productFromId(Long id) {
        if (id == null) return null;
        Product p = new Product();
        p.setId(id);
        return p;
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

    /** Calcula precio con descuento (considerando específicos y globales) */
    default BigDecimal computeDiscountedPrice(Product p, @Context List<Discount> globalDiscounts) {
        BigDecimal base = computeRepresentativePrice(p);
        if (base == null) return null;

        BigDecimal best = base;

        // 1. Evaluar descuentos específicos del producto
        if (p.getDiscounts() != null) {
            for (Discount d : p.getDiscounts()) {
                best = applyIfBetter(base, best, d);
            }
        }

        // 2. Evaluar descuentos globales (sitio o tipo de producto)
        if (globalDiscounts != null) {
            for (Discount d : globalDiscounts) {
                best = applyIfBetter(base, best, d);
            }
        }

        return best.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal applyIfBetter(BigDecimal base, BigDecimal currentBest, Discount d) {
        BigDecimal pct = d.getPercentage();
        if (pct != null && pct.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal off = base.multiply(pct).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal candidate = base.subtract(off);
            if (candidate.compareTo(currentBest) < 0) return candidate;
        }
        // TODO: implementar monto fijo aquí si es necesario
        return currentBest;
    }

    /** Calcula precio con transferencia usando porcentaje dinámico */
    default BigDecimal computePriceWithTransfer(Product p, @Context List<Discount> globalDiscounts, @Context BigDecimal transferDiscountPct) {
        BigDecimal discounted = computeDiscountedPrice(p, globalDiscounts);
        if (discounted == null) return null;

        if (transferDiscountPct == null || transferDiscountPct.compareTo(BigDecimal.ZERO) <= 0) {
            return discounted;
        }

        BigDecimal off = discounted.multiply(transferDiscountPct).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return discounted.subtract(off).setScale(2, RoundingMode.HALF_UP);
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
