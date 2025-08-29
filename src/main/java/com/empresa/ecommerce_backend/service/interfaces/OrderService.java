// src/main/java/com/empresa/ecommerce_backend/service/interfaces/OrderService.java
package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.dto.request.CreateOrderRequest;
import com.empresa.ecommerce_backend.dto.request.UpdatePaymentMethodRequest;
import com.empresa.ecommerce_backend.dto.response.OrderResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;

import java.util.List;

public interface OrderService {
    ServiceResult<OrderResponse> createOrder(CreateOrderRequest req);
    ServiceResult<OrderResponse> getOne(Long id);
    ServiceResult<List<OrderResponse>> listMine();
    ServiceResult<OrderResponse> patchPaymentMethod(Long orderId, UpdatePaymentMethodRequest req);
}
