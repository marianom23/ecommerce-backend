// src/main/java/com/empresa/ecommerce_backend/repository/projection/PriceRangeProjection.java
package com.empresa.ecommerce_backend.repository.projection;

import java.math.BigDecimal;

public interface PriceRangeProjection {
    BigDecimal getMinPrice();
    BigDecimal getMaxPrice();
}
