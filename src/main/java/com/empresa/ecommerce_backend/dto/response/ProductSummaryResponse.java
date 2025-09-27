// src/main/java/com/empresa/ecommerce_backend/dto/response/wishlist/ProductSummaryResponse.java
package com.empresa.ecommerce_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class ProductSummaryResponse {
    private final Long id;
    private final String name;
    private final String imageUrl;     // imagen general del producto
    private final BigDecimal minPrice; // mínimo entre variantes, si aplica
}
