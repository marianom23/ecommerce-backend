// src/main/java/com/empresa/ecommerce_backend/dto/response/OrderSummaryResponse.java
package com.empresa.ecommerce_backend.dto.response;

import com.empresa.ecommerce_backend.enums.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderSummaryResponse {
    private Long id;
    private String orderNumber;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private Integer itemCount;        // útil para la UI
    private String firstItemThumb;    // opcional (si tenés imágenes)
}
