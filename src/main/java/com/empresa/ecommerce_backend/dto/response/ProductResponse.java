package com.empresa.ecommerce_backend.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductResponse {
    private Long id;
    private String title;
    private int reviews;
    private BigDecimal price;
    private BigDecimal discountedPrice;
    private ImagesDto imgs;

    @Data
    public static class ImagesDto {
        private List<String> thumbnails;
        private List<String> previews;
    }
}
