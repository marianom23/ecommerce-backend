// CartItemResponse.java
package com.empresa.ecommerce_backend.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class CartItemResponse {
    private final Long id;
    private final Long productId;
    private final Long variantId;
    private final String name;
    private final String imageUrl;
    private final String attributesJson;
    private final BigDecimal unitPrice;
    private final BigDecimal unitDiscountedPrice;
    private final BigDecimal priceAtAddition;
    private final BigDecimal discountedPriceAtAddition;
    private final Integer quantity;
    private final BigDecimal subtotal;
}