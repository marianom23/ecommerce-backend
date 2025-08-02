// src/main/java/com/empresa/ecommerce_backend/service/interfaces/CartService.java
package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.dto.request.AddItemRequest;
import com.empresa.ecommerce_backend.dto.request.UpdateQtyRequest;
import com.empresa.ecommerce_backend.dto.response.CartResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;

public interface CartService {

    ServiceResult<CartResponse> getOrCreateBySession(String sessionId);

    ServiceResult<CartResponse> addItem(String sessionId, AddItemRequest dto);

    ServiceResult<CartResponse> updateQuantity(String sessionId, Long itemId, UpdateQtyRequest dto);

    ServiceResult<CartResponse> removeItem(String sessionId, Long itemId);

    ServiceResult<CartResponse> clear(String sessionId);
}
