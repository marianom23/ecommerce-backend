// src/main/java/com/empresa/ecommerce_backend/service/interfaces/OrderService.java
package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.dto.request.ConfirmOrderRequest;
import com.empresa.ecommerce_backend.dto.request.CreateOrderRequest;
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
    ServiceResult<OrderResponse> createOrder(CreateOrderRequest req);

    ServiceResult<OrderResponse> getOne(Long id);

    // EXISTENTE (lista completa)
    ServiceResult<List<OrderResponse>> listMine();

    ServiceResult<OrderResponse> getOneByNumber(String orderNumber);

    // NUEVO: lista resumida paginada
    ServiceResult<PageResponse<OrderSummaryResponse>> listMineSummaries(Pageable pageable);

    ServiceResult<OrderResponse> patchShippingAddress(String orderNumber, UpdateShippingAddressRequest req);

    ServiceResult<OrderResponse> patchBillingProfile(String orderNumber, UpdateBillingProfileRequest req);

    ServiceResult<OrderResponse> patchPaymentMethod(String orderNumber, UpdatePaymentMethodRequest req);

    ServiceResult<OrderResponse> confirmOrder(String orderNumber, ConfirmOrderRequest req);

    ServiceResult<OrderResponse> updateOrderStatus(Long orderId, UpdateOrderStatusRequest req);

    // Admin backoffice
    ServiceResult<PageResponse<OrderBackofficeResponse>> listAllOrdersForBackoffice(
            Pageable pageable, String search, OrderStatus orderStatus, PaymentStatus paymentStatus);

    ServiceResult<OrderResponse> getOrderByIdForAdmin(Long orderId);

    ServiceResult<OrderResponse> updateOrderStatusForAdmin(Long orderId, UpdateOrderStatusRequest req);

    // Guest checkout
    ServiceResult<OrderResponse> getGuestOrder(String email, String orderNumber);

    // Link guest orders to user
    void linkGuestOrdersToUser(String email, Long userId);
}
