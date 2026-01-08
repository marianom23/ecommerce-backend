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
    private final com.empresa.ecommerce_backend.service.MetaPixelService metaPixelService;

    // Usuario confirma que hizo la transferencia
    @PostMapping("/orders/{orderId}/bank-transfer/confirm")
    public ServiceResult<OrderResponse> userConfirmBankTransfer(
            @PathVariable Long orderId,
            @RequestBody(required = false) BankTransferUserConfirmRequest req
    ) {
        // Obtener userId desde SecurityContext
        Long userId = getCurrentUserId();
        String reference = req != null ? req.getReference() : null;
        String receiptUrl = req != null ? req.getReceiptUrl() : null;
        return paymentService.confirmBankTransferByUser(orderId, userId, reference, receiptUrl);
    }

    // Admin aprueba/rechaza la transferencia
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/orders/{orderId}/bank-transfer/review")
    public ServiceResult<OrderResponse> adminReviewBankTransfer(
            @PathVariable Long orderId,
            @RequestBody BankTransferAdminReviewRequest req,
            jakarta.servlet.http.HttpServletRequest request
    ) {
        ServiceResult<OrderResponse> result = paymentService.reviewBankTransferByAdmin(orderId, req.isApprove(), req.getNote());
        
        // 📊 Si se aprobó, enviar evento Purchase a Meta
        if (req.isApprove() && result.getData() != null) {
            OrderResponse order = result.getData();
            metaPixelService.sendEvent(
                "Purchase",
                request,
                null, // Admin action, no user context
                order.getTotalAmount().doubleValue(),
                "ARS",
                "order-" + order.getOrderNumber() // Event ID for deduplication
            );
        }
        
        return result;
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
    public String mpWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestHeader Map<String, String> headers,
            jakarta.servlet.http.HttpServletRequest request
    ) {
        // TODO: validar firma
        com.empresa.ecommerce_backend.model.Order order = paymentService.handleGatewayWebhook("MERCADO_PAGO", payload);
        
        // 📊 Si el pago fue aprobado, enviar evento Purchase a Meta
        if (order != null) {
            metaPixelService.sendEvent(
                "Purchase",
                request,
                order.getUser(),
                order.getTotalAmount().doubleValue(),
                "ARS",
                "order-" + order.getOrderNumber() // Event ID for deduplication
            );
        }
        
        return "ok";
    }

    private Long getCurrentUserId() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalArgumentException("No autenticado.");
        }
        try {
            return (Long) auth.getPrincipal().getClass().getMethod("getId").invoke(auth.getPrincipal());
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo obtener ID de usuario autenticado");
        }
    }
}
