// src/main/java/com/empresa/ecommerce_backend/dto/request/UpdateShippingAddressRequest.java
package com.empresa.ecommerce_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateShippingAddressRequest {
    @NotNull
    private Long shippingAddressId;

    // opcionales para el snapshot
    private String recipientName;
    private String phone;
}
