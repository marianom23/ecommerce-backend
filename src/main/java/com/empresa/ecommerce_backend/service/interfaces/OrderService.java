// src/main/java/com/empresa/ecommerce_backend/service/interfaces/OrderService.java
package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.dto.request.ConfirmOrderRequest;
import com.empresa.ecommerce_backend.dto.request.UpdateBillingProfileRequest;
import com.empresa.ecommerce_backend.dto.request.UpdatePaymentMethodRequest;
import com.empresa.ecommerce_backend.dto.request.UpdateShippingAddressRequest;
import com.empresa.ecommerce_backend.dto.response.OrderResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;

import java.util.List;

public interface OrderService {
    ServiceResult<OrderResponse> createOrder(); // SIN body
    ServiceResult<OrderResponse> getOne(Long id);
    ServiceResult<List<OrderResponse>> listMine();
    ServiceResult<OrderResponse> patchShippingAddress(Long orderId, UpdateShippingAddressRequest req);
    ServiceResult<OrderResponse> patchBillingProfile(Long orderId, UpdateBillingProfileRequest req);
    ServiceResult<OrderResponse> patchPaymentMethod(Long orderId, UpdatePaymentMethodRequest req);

    ServiceResult<OrderResponse> confirmOrder(Long orderId, ConfirmOrderRequest req);
}
