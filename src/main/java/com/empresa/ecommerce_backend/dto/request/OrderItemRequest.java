// src/main/java/com/empresa/ecommerce_backend/dto/request/OrderItemRequest.java
package com.empresa.ecommerce_backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItemRequest {
    @NotNull
    private Long variantId;

    @NotNull @Min(1)
    private Integer quantity;
}
