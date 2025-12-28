package com.empresa.ecommerce_backend.dto.request;

import com.empresa.ecommerce_backend.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    @NotNull(message = "El estado es requerido")
    private OrderStatus status;
}
