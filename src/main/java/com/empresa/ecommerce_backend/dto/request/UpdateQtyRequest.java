// UpdateQtyRequest.java
package com.empresa.ecommerce_backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class UpdateQtyRequest {
    @NotNull @Min(0) private Integer quantity; // 0 = eliminar
}