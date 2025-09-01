// src/main/java/com/empresa/ecommerce_backend/service/interfaces/PaymentService.java
package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.dto.response.OrderResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.enums.PaymentMethod;
import com.empresa.ecommerce_backend.model.Order;

import java.util.Map;

public interface PaymentService {

    ServiceResult<OrderResponse> initPaymentForOrder(Order order, PaymentMethod method);

    ServiceResult<OrderResponse> confirmBankTransferByUser(Long orderId, Long userId, String reference, String receiptUrl);

    ServiceResult<OrderResponse> reviewBankTransferByAdmin(Long orderId, boolean approve, String note);

    ServiceResult<OrderResponse> cancelPayment(Long orderId, String triggeredBy, String note);

    void handleGatewayWebhook(String provider, Map<String, Object> payload);

    int expireOverduePayments();
}
