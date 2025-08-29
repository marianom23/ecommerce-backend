// src/main/java/com/empresa/ecommerce_backend/dto/request/UpdatePaymentMethodRequest.java
package com.empresa.ecommerce_backend.dto.request;

import com.empresa.ecommerce_backend.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdatePaymentMethodRequest {
    @NotNull
    private PaymentMethod paymentMethod;
}
