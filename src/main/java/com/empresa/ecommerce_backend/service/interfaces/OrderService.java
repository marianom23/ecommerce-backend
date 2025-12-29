// src/main/java/com/empresa/ecommerce_backend/service/interfaces/OrderService.java
package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.dto.request.ConfirmOrderRequest;
import com.empresa.ecommerce_backend.dto.request.UpdateBillingProfileRequest;
import com.empresa.ecommerce_backend.dto.request.UpdateOrderStatusRequest;
import com.empresa.ecommerce_backend.dto.request.UpdatePaymentMethodRequest;
import com.empresa.ecommerce_backend.dto.request.UpdateShippingAddressRequest;
import com.empresa.ecommerce_backend.dto.response.*;
import com.empresa.ecommerce_backend.enums.OrderStatus;
import com.empresa.ecommerce_backend.enums.PaymentStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    ServiceResult<OrderResponse> createOrder();
    ServiceResult<OrderResponse> getOne(Long id);

    // EXISTENTE (lista completa)
    ServiceResult<List<OrderResponse>> listMine();
    ServiceResult<OrderResponse> getOneByNumber(String orderNumber);
    // NUEVO: lista resumida paginada
    ServiceResult<PageResponse<OrderSummaryResponse>> listMineSummaries(Pageable pageable);

    ServiceResult<OrderResponse> patchShippingAddress(Long orderId, UpdateShippingAddressRequest req);
    ServiceResult<OrderResponse> patchBillingProfile(Long orderId, UpdateBillingProfileRequest req);
    ServiceResult<OrderResponse> patchPaymentMethod(Long orderId, UpdatePaymentMethodRequest req);
    ServiceResult<OrderResponse> confirmOrder(Long orderId, ConfirmOrderRequest req);
    ServiceResult<OrderResponse> updateOrderStatus(Long orderId, UpdateOrderStatusRequest req);
    
    // Admin backoffice
    ServiceResult<PageResponse<OrderBackofficeResponse>> listAllOrdersForBackoffice(
            Pageable pageable, String search, OrderStatus orderStatus, PaymentStatus paymentStatus);
}
