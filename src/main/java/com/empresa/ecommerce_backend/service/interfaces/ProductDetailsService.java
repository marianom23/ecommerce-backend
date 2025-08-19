// src/main/java/com/empresa/ecommerce_backend/service/interfaces/ProductDetailsService.java
package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.dto.response.ProductDetailsResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;

public interface ProductDetailsService {
    ServiceResult<ProductDetailsResponse> getDetails(Long productId);
}