// dto/response/PriceRangeResponse.java
package com.empresa.ecommerce_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data @AllArgsConstructor
public class PriceRangeResponse {
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
