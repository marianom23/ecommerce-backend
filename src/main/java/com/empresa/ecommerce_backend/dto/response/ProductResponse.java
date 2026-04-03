package com.empresa.ecommerce_backend.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductResponse {
    private Long id;
    private String title;
    private Double averageRating;
    private Long totalReviews;
    private BigDecimal price;
    private BigDecimal discountedPrice;
    private BigDecimal priceWithTransfer;
    private ImagesDto imgs;

    private int variantCount;        // 👈 cantidad de variantes
    private Long defaultVariantId;   // 👈 id de la variante única (si variantCount == 1)
    private String fulfillmentType;
    private String consoleName;
    private String productType;

    @Data
    public static class ImagesDto {
        private List<String> urls;
    }
}
