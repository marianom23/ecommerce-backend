package com.empresa.ecommerce_backend.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String sku;
    private String imageUrl;
    private String brandName;
    private String categoryName;
}
