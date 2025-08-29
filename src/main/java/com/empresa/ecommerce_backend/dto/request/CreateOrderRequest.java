// src/main/java/com/empresa/ecommerce_backend/dto/request/CreateOrderRequest.java
package com.empresa.ecommerce_backend.dto.request;

import com.empresa.ecommerce_backend.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotNull
    private Long shippingAddressId;

    @NotNull
    private Long billingProfileId;

    @Valid
    @Size(min = 1)
    @NotNull
    private List<OrderItemRequest> items;

    // Opcionales
    private String couponCode;
    private String orderNote;

    // 🔑 Nuevo: método de pago elegido en el checkout
    @NotNull
    private PaymentMethod paymentMethod;
}
