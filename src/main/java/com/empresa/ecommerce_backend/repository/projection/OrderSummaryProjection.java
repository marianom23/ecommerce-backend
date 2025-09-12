// src/main/java/com/empresa/ecommerce_backend/repository/projection/OrderSummaryProjection.java
package com.empresa.ecommerce_backend.repository.projection;

import com.empresa.ecommerce_backend.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface OrderSummaryProjection {
    Long getId();
    String getOrderNumber();
    LocalDateTime getOrderDate();
    OrderStatus getStatus();
    BigDecimal getTotalAmount();
    Integer getItemCount();
    String getFirstItemThumb(); // si no lo tenés, quedará null
}
