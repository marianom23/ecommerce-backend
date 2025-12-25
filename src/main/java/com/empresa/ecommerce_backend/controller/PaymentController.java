// src/main/java/com/empresa/ecommerce_backend/controller/PaymentController.java
package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.request.BankTransferAdminReviewRequest;
import com.empresa.ecommerce_backend.dto.request.BankTransferUserConfirmRequest;
import com.empresa.ecommerce_backend.dto.response.OrderResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.service.interfaces.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // Usuario confirma que hizo la transferencia
    @PostMapping("/orders/{orderId}/bank-transfer/confirm")
    public ServiceResult<OrderResponse> userConfirmBankTransfer(
            @PathVariable Long orderId,
            @RequestBody(required = false) BankTransferUserConfirmRequest req
    ) {
        // userId desde el contexto (idealmente en un SecurityService)
        Long userId = /* TODO: SecurityContext */ null;
        String reference = req != null ? req.getReference() : null;
        String receiptUrl = req != null ? req.getReceiptUrl() : null;
        return paymentService.confirmBankTransferByUser(orderId, userId, reference, receiptUrl);
    }

    // Admin aprueba/rechaza la transferencia
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/orders/{orderId}/bank-transfer/review")
    public ServiceResult<OrderResponse> adminReviewBankTransfer(
            @PathVariable Long orderId,
            @RequestBody BankTransferAdminReviewRequest req
    ) {
        return paymentService.reviewBankTransferByAdmin(orderId, req.isApprove(), req.getNote());
    }

    // Cancelar pago (user/admin)
    @PostMapping("/orders/{orderId}/cancel")
    public ServiceResult<OrderResponse> cancelPayment(
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String note = body != null ? body.get("note") : null;
        String who = "user"; // o "admin" según endpoint/rol
        return paymentService.cancelPayment(orderId, who, note);
    }

    // Webhook Mercado Pago (sin ServiceResult; solo 200/OK)
    @PostMapping("/webhook/mercadopago")
    public String mpWebhook(@RequestBody Map<String, Object> payload, @RequestHeader Map<String, String> headers) {
        // TODO: validar firma
        paymentService.handleGatewayWebhook("MERCADO_PAGO", payload);
        return "ok";
    }
}
