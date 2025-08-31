// src/main/java/com/empresa/ecommerce_backend/dto/request/UpdateBillingProfileRequest.java
package com.empresa.ecommerce_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateBillingProfileRequest {
    @NotNull
    private Long billingProfileId;
}
