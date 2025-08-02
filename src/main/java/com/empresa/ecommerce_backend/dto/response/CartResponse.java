// CartResponse.java
package com.empresa.ecommerce_backend.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CartResponse {
    private final Long id;
    private final String sessionId;
    private final LocalDateTime updatedAt;
    private final List<CartItemResponse> items;
    private final CartTotalsResponse totals;
}