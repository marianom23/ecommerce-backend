package com.empresa.ecommerce_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class ProductVariantResponse {
    private final Long id;
    private final Long productId;
    private final String sku;
    private final BigDecimal price;
    private final Integer stock;
    private final String attributesJson;
}