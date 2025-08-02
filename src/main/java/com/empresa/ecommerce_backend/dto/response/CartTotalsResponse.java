// CartTotalsResponse.java
package com.empresa.ecommerce_backend.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class CartTotalsResponse {
    private final BigDecimal itemsSubtotal;
    private final BigDecimal grandTotal;
}