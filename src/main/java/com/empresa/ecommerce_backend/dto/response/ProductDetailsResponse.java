package com.empresa.ecommerce_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // ← oculta campos null en la respuesta
public class ProductDetailsResponse {
    private Long id;
    private String title;
    private int reviews;
    private BigDecimal price;
    private BigDecimal discountedPrice;
    private ImagesDto imgs;

    private String description;
    private String sku;
    private String brand;
    private String category;

    private BigDecimal weight;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;

    private List<String> tags;
    private List<DiscountDto> discounts;

    // ← NUEVO: solo se completa si el producto es SIMPLE
    private Integer stock;

    private boolean hasVariants;
    private List<String> variantAttributes;
    private Map<String, List<String>> variantOptions;
    private List<VariantDto> variants;

    @Data
    public static class ImagesDto {
        private List<String> thumbnails;
        private List<String> previews;
    }

    @Data
    public static class DiscountDto {
        private Long id;
        private String name;
        private String type;
        private BigDecimal value;
    }

    @Data
    public static class VariantDto {
        private Long id;
        private String sku;
        private BigDecimal price;
        private BigDecimal discountedPrice;
        private Integer stock;
        private Map<String, String> attributes;
        private ImagesDto imgs;
    }
}
