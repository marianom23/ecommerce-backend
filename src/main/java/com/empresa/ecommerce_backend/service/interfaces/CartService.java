// src/main/java/com/empresa/ecommerce_backend/service/interfaces/CartService.java
package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.dto.request.AddItemRequest;
import com.empresa.ecommerce_backend.dto.request.UpdateQtyRequest;
import com.empresa.ecommerce_backend.dto.response.CartResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;

public interface CartService {

    ServiceResult<CartResponse> attachCartToUser(String sessionId, Long userId);

    // ⚠️ NUEVO: user-first (si userId != null opera por usuario, sino por sessionId)
    ServiceResult<CartResponse> getOrCreate(Long userId, String sessionId);

    ServiceResult<CartResponse> addItem(Long userId, String sessionId, AddItemRequest dto);

    ServiceResult<CartResponse> updateQuantity(Long userId, String sessionId, Long itemId, UpdateQtyRequest dto);

    ServiceResult<CartResponse> incrementItem(Long userId, String sessionId, Long itemId);

    ServiceResult<CartResponse> decrementItem(Long userId, String sessionId, Long itemId);

    ServiceResult<CartResponse> removeItem(Long userId, String sessionId, Long itemId);

    ServiceResult<CartResponse> clear(Long userId, String sessionId);

    boolean userHasCart(Long userId);
}
