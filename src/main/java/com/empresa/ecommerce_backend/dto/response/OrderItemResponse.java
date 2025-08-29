// src/main/java/com/empresa/ecommerce_backend/dto/response/OrderItemResponse.java
package com.empresa.ecommerce_backend.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItemResponse {
    private Long productId;        // del Product padre
    private Long variantId;        // de la variante
    private String productName;    // snapshot guardado en OrderItem
    private String sku;            // snapshot
    private String attributesJson; // snapshot de atributos de la variante
    private BigDecimal unitPrice;  // snapshot
    private Integer quantity;
    private BigDecimal discountAmount;
    private BigDecimal lineTotal;
}