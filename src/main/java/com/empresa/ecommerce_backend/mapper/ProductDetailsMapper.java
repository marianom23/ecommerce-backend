package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.dto.response.ProductDetailsResponse;
import com.empresa.ecommerce_backend.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductDetailsMapper {

    @Mapping(target = "title", source = "product.name")
    @Mapping(target = "reviews", expression = "java(0)")
    // precios representativos desde variantes
    @Mapping(target = "price",           expression = "java(minVariantPrice(variants))")
    @Mapping(target = "discountedPrice", expression = "java(discountedMinVariantPrice(product, variants))")
    // imágenes del producto (no de variante)
    @Mapping(target = "imgs", expression = "java(buildProductImages(product))")
    // metadatos
    @Mapping(target = "brand",    expression = "java(product.getBrand()    != null ? product.getBrand().getName()    : null)")
    @Mapping(target = "category", expression = "java(product.getCategory() != null ? product.getCategory().getName() : null)")
    @Mapping(target = "tags",     expression = "java(mapTags(product.getTags()))")
    @Mapping(target = "discounts",expression = "java(mapDiscounts(product.getDiscounts()))")
    // medidas
    @Mapping(target = "weight", source = "product.weight")
    @Mapping(target = "length", source = "product.length")
    @Mapping(target = "width",  source = "product.width")
    @Mapping(target = "height", source = "product.height")
    @Mapping(target = "sku",    source = "product.sku")
    @Mapping(target = "description", source = "product.description")
    // variantes
    @Mapping(target = "hasVariants",       expression = "java(variants != null && !variants.isEmpty())")
    @Mapping(target = "variantAttributes", expression = "java(buildVariantAttributes(variants))")
    @Mapping(target = "variantOptions",    expression = "java(buildVariantOptions(variants))")
    @Mapping(target = "variants",          expression = "java(mapVariants(product, variants))")
    // stock: suma de stocks de variantes (Product ya no tiene stock)
    @Mapping(target = "stock",             expression = "java(totalVariantStock(variants))")
    ProductDetailsResponse toDetails(Product product, java.util.List<ProductVariant> variants);

    // ---------- Precio representativo (mínimo entre variantes) ----------
    default BigDecimal minVariantPrice(List<ProductVariant> variants) {
        if (variants == null || variants.isEmpty()) return null;
        return variants.stream()
                .map(ProductVariant::getPrice)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(null);
    }

    // Descuento aplicado al precio representativo mínimo
    default BigDecimal discountedMinVariantPrice(Product product, List<ProductVariant> variants) {
        BigDecimal base = minVariantPrice(variants);
        if (base == null) return null;
        return applyBestDiscount(base, product != null ? product.getDiscounts() : null);
    }

    // ---------- Stock total (suma de variantes) ----------
    default Integer totalVariantStock(List<ProductVariant> variants) {
        if (variants == null || variants.isEmpty()) return null; // o 0 si lo preferís
        long sum = variants.stream()
                .map(ProductVariant::getStock)
                .filter(Objects::nonNull)
                .mapToLong(Integer::longValue)
                .sum();
        // evita overflow al castear
        return (sum > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) sum;
    }

    // ---------- Imágenes ----------
    default ProductDetailsResponse.ImagesDto buildProductImages(Product p) {
        List<String> all = (p != null && p.getImages() != null)
                ? p.getImages().stream()
                .filter(Objects::nonNull)
                .filter(pi -> pi.getVariant() == null)
                .sorted(Comparator.comparing(ProductImage::getPosition, Comparator.nullsLast(Integer::compareTo)))
                .map(ProductImage::getUrl)
                .filter(Objects::nonNull)
                .toList()
                : List.of();

        ProductDetailsResponse.ImagesDto imgs = new ProductDetailsResponse.ImagesDto();
        imgs.setThumbnails(all.stream().limit(4).toList());
        imgs.setPreviews(all);
        return imgs;
    }

    default ProductDetailsResponse.ImagesDto buildVariantImages(ProductVariant v) {
        List<String> all = (v != null && v.getImages() != null)
                ? v.getImages().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ProductImage::getPosition, Comparator.nullsLast(Integer::compareTo)))
                .map(ProductImage::getUrl)
                .filter(Objects::nonNull)
                .toList()
                : List.of();

        ProductDetailsResponse.ImagesDto imgs = new ProductDetailsResponse.ImagesDto();
        imgs.setThumbnails(all.stream().limit(4).toList());
        imgs.setPreviews(all);
        return imgs;
    }

    // ---------- Variantes ----------
    default List<String> buildVariantAttributes(List<ProductVariant> variants) {
        if (variants == null || variants.isEmpty()) return List.of();
        return variants.stream()
                .map(this::parseAttributesJson)
                .filter(Objects::nonNull)
                .flatMap(m -> m.keySet().stream())
                .distinct()
                .sorted()
                .toList();
    }

    default Map<String, List<String>> buildVariantOptions(List<ProductVariant> variants) {
        if (variants == null || variants.isEmpty()) return Map.of();

        Map<String, Set<String>> tmp = new LinkedHashMap<>();
        for (ProductVariant v : variants) {
            Map<String, String> attrs = parseAttributesJson(v);
            if (attrs == null) continue;
            for (Map.Entry<String, String> e : attrs.entrySet()) {
                tmp.computeIfAbsent(e.getKey(), k -> new LinkedHashSet<>()).add(e.getValue());
            }
        }

        Map<String, List<String>> result = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> e : tmp.entrySet()) {
            List<String> values = new ArrayList<>(e.getValue());
            Collections.sort(values);
            result.put(e.getKey(), values);
        }
        return result;
    }

    default List<ProductDetailsResponse.VariantDto> mapVariants(Product product, List<ProductVariant> variants) {
        if (variants == null || variants.isEmpty()) return List.of();

        Set<Discount> productDiscounts = (product != null) ? product.getDiscounts() : null;

        return variants.stream()
                .map(v -> {
                    ProductDetailsResponse.VariantDto dto = new ProductDetailsResponse.VariantDto();
                    dto.setId(v.getId());
                    dto.setSku(v.getSku());
                    dto.setPrice(v.getPrice());
                    dto.setDiscountedPrice(applyBestDiscount(v.getPrice(), productDiscounts)); // descuentos del producto
                    dto.setStock(v.getStock());
                    dto.setAttributes(parseAttributesJson(v));
                    dto.setImgs(buildVariantImages(v));
                    return dto;
                })
                .toList();
    }

    // ---------- Tags / Descuentos ----------
    default List<String> mapTags(Set<Tag> tags) {
        if (tags == null || tags.isEmpty()) return List.of();
        return tags.stream()
                .map(Tag::getName)
                .filter(Objects::nonNull)
                .sorted()
                .toList();
    }

    default List<ProductDetailsResponse.DiscountDto> mapDiscounts(Set<Discount> discounts) {
        if (discounts == null || discounts.isEmpty()) return List.of();

        List<ProductDetailsResponse.DiscountDto> list = new ArrayList<>();
        for (Discount d : discounts) {
            ProductDetailsResponse.DiscountDto dto = new ProductDetailsResponse.DiscountDto();
            dto.setId(d.getId());
            dto.setName(d.getName());

            BigDecimal percent = getPercentage(d);
            BigDecimal amount  = getFixedAmount(d);

            if (percent != null && percent.compareTo(BigDecimal.ZERO) > 0) {
                dto.setType("PERCENT");
                dto.setValue(percent);
            } else if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
                dto.setType("FIXED");
                dto.setValue(amount);
            } else {
                dto.setType("PERCENT");
                dto.setValue(BigDecimal.ZERO);
            }
            list.add(dto);
        }
        return list;
    }

    // ---------- Descuentos ----------
    default BigDecimal applyBestDiscount(BigDecimal base, Set<Discount> discounts) {
        if (base == null) return null;
        if (discounts == null || discounts.isEmpty()) return base.setScale(2, RoundingMode.HALF_UP);

        BigDecimal best = base;
        for (Discount d : discounts) {
            BigDecimal candidate = base;

            BigDecimal percent = getPercentage(d);
            BigDecimal amount  = getFixedAmount(d);

            if (percent != null && percent.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal off = base.multiply(percent).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                candidate = base.subtract(off);
            } else if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
                candidate = base.subtract(amount);
            }

            if (candidate.compareTo(best) < 0) best = candidate;
        }
        return best.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    // Ajustá estos getters a tu entidad Discount
    private static BigDecimal getPercentage(Discount d) {
        try { return d.getPercentage(); } catch (Exception e) { return null; }
    }
    private static BigDecimal getFixedAmount(Discount d) {
        try { /* return d.getAmount(); */ return null; } catch (Exception e) { return null; }
    }

    // ---------- Parseo de attributesJson (Variant) ----------
    default Map<String, String> parseAttributesJson(ProductVariant v) {
        if (v == null || v.getAttributesJson() == null || v.getAttributesJson().isBlank()) return Map.of();
        try {
            return new ObjectMapper().readValue(
                    v.getAttributesJson(),
                    new TypeReference<Map<String, String>>() {}
            );
        } catch (Exception e) {
            return Map.of();
        }
    }
}
