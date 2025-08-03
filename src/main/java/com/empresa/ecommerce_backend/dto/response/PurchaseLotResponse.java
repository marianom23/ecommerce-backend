package com.empresa.ecommerce_backend.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseLotResponse {
    private Long id;
    private Long productId;
    private Long productVariantId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitCost;
    private BigDecimal taxPercentage;
    private BigDecimal totalCost;
    private String notes;
}
