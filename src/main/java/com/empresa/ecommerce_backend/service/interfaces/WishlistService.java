// src/main/java/com/empresa/ecommerce_backend/service/interfaces/WishlistService.java
package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.dto.response.WishlistResponse;

public interface WishlistService {

    ServiceResult<WishlistResponse> getOrCreateForUser(Long userId);

    ServiceResult<WishlistResponse> addProduct(Long userId, Long productId);

    ServiceResult<WishlistResponse> removeProduct(Long userId, Long productId);

    ServiceResult<WishlistResponse> toggleProduct(Long userId, Long productId);

    ServiceResult<WishlistResponse> clear(Long userId);
}
