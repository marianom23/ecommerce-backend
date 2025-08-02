// AddItemRequest.java
package com.empresa.ecommerce_backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class AddItemRequest {
    @NotNull private Long productId;
    private Long variantId;
    @NotNull @Min(1) private Integer quantity;
}