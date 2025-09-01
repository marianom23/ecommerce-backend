// src/main/java/com/empresa/ecommerce_backend/service/PaymentServiceImpl.java
package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.response.OrderResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.enums.OrderStatus;
import com.empresa.ecommerce_backend.enums.PaymentMethod;
import com.empresa.ecommerce_backend.enums.PaymentStatus;
import com.empresa.ecommerce_backend.exception.RecursoNoEncontradoException;
import com.empresa.ecommerce_backend.mapper.OrderMapper;
import com.empresa.ecommerce_backend.model.Order;
import com.empresa.ecommerce_backend.model.Payment;
import com.empresa.ecommerce_backend.model.PaymentEvent;
import com.empresa.ecommerce_backend.repository.*;
import com.empresa.ecommerce_backend.service.interfaces.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepo;
    private final PaymentRepository paymentRepo;
    private final PaymentEventRepository paymentEventRepo;
    private final ProductVariantRepository variantRepo;
    private final OrderMapper orderMapper;

    @Value("${mp.access-token}")
    private String mpAccessToken;

    @Value("${mp.webhook-url}")
    private String mpWebhookUrl;

    @Override
    @Transactional
    public ServiceResult<OrderResponse> initPaymentForOrder(Order o, PaymentMethod method) {
        if (o.getPayment() != null && o.getPayment().getStatus() != PaymentStatus.CANCELED) {
            return ServiceResult.ok(orderMapper.toResponse(o));
        }

        Payment p = new Payment();
        p.setOrder(o);
        p.setAmount(o.getTotalAmount());
        p.setMethod(method);
        p.setStatus(PaymentStatus.INITIATED);
        p.setExpiresAt(LocalDateTime.now().plusHours(1));

        try {
            if (method == PaymentMethod.MERCADO_PAGO || method == PaymentMethod.CARD) {
                initMercadoPago(p, o);
            }
        } catch (IllegalStateException ex) {
            return ServiceResult.error(HttpStatus.BAD_GATEWAY,
                    "No se pudo iniciar el pago con Mercado Pago: " + ex.getMessage());
        }

        paymentRepo.save(p);
        o.setPayment(p);
        o.setStatus(OrderStatus.CONFIRMED);
        orderRepo.save(o);

        saveEvent(p, null, PaymentStatus.INITIATED, "system", "payment initiated");
        return ServiceResult.ok(orderMapper.toResponse(o));
    }



    @Override
    @Transactional
    public ServiceResult<OrderResponse> confirmBankTransferByUser(Long orderId, Long userId, String reference, String receiptUrl) {
        Order o = orderRepo.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
        Payment p = mustPayment(o, PaymentMethod.BANK_TRANSFER);

        if (p.getStatus() != PaymentStatus.INITIATED) {
            return ServiceResult.ok(orderMapper.toResponse(o)); // idempotencia
        }
        if (isExpired(p)) {
            return ServiceResult.error(HttpStatus.GONE, "El tiempo para confirmar expiró.");
        }

        var prev = p.getStatus();
        p.setStatus(PaymentStatus.PENDING);
        p.setTransferReference(reference);
        p.setReceiptUrl(receiptUrl);
        p.setExpiresAt(LocalDateTime.now().plusHours(48)); // ventana para revisión admin
        paymentRepo.save(p);

        if (o.getStatus() == OrderStatus.PENDING) {
            o.setStatus(OrderStatus.CONFIRMED);
            orderRepo.save(o);
        }

        saveEvent(p, prev, PaymentStatus.PENDING, "user", "user marked bank transfer done");
        return ServiceResult.ok(orderMapper.toResponse(o));
    }

    @Override
    @Transactional
    public ServiceResult<OrderResponse> reviewBankTransferByAdmin(Long orderId, boolean approve, String note) {
        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
        Payment p = mustPayment(o, PaymentMethod.BANK_TRANSFER);

        if (p.getStatus() != PaymentStatus.PENDING) {
            return ServiceResult.ok(orderMapper.toResponse(o)); // idempotencia
        }

        var prev = p.getStatus();
        p.setStatus(approve ? PaymentStatus.APPROVED : PaymentStatus.REJECTED);
        p.setExpiresAt(null);
        paymentRepo.save(p);

        if (approve) {
            o.setStatus(OrderStatus.PAID);
        } else {
            rollbackStock(o);
            o.setStatus(OrderStatus.CANCELED);
        }
        orderRepo.save(o);

        saveEvent(p, prev, p.getStatus(), "admin", note);
        return ServiceResult.ok(orderMapper.toResponse(o));
    }

    @Override
    @Transactional
    public ServiceResult<OrderResponse> cancelPayment(Long orderId, String who, String note) {
        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
        Payment p = o.getPayment();
        if (p == null) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST, "La orden no tiene pago asociado.");
        }
        if (p.getStatus() == PaymentStatus.APPROVED || p.getStatus() == PaymentStatus.CANCELED) {
            return ServiceResult.ok(orderMapper.toResponse(o)); // nada para hacer
        }

        var prev = p.getStatus();
        p.setStatus(PaymentStatus.CANCELED);
        p.setExpiresAt(null);
        paymentRepo.save(p);

        rollbackStock(o);
        o.setStatus(OrderStatus.CANCELED);
        orderRepo.save(o);

        saveEvent(p, prev, PaymentStatus.CANCELED, who, note);
        return ServiceResult.ok(orderMapper.toResponse(o));
    }

    @Override
    @Transactional
    public void handleGatewayWebhook(String provider, Map<String, Object> payload) {
        String providerPaymentId = extractProviderPaymentId(provider, payload);
        Payment p = paymentRepo.findByProviderAndProviderPaymentId(provider, providerPaymentId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Payment no encontrado"));
        PaymentStatus newStatus = mapGatewayStatus(provider, payload);
        if (newStatus == null || p.getStatus() == newStatus) return;

        var prev = p.getStatus();
        p.setStatus(newStatus);
        p.setExpiresAt(null);
        paymentRepo.save(p);
        saveEvent(p, prev, newStatus, "webhook:"+provider, null);

        Order o = p.getOrder();
        switch (newStatus) {
            case APPROVED -> {
                o.setStatus(OrderStatus.PAID);
                orderRepo.save(o);
            }
            case REJECTED, CANCELED, EXPIRED -> {
                rollbackStock(o);
                o.setStatus(OrderStatus.CANCELED);
                orderRepo.save(o);
            }
            default -> {}
        }
    }

    @Override
    @Transactional
    public int expireOverduePayments() {
        var now = LocalDateTime.now();
        var list = paymentRepo.findAllByStatusInAndExpiresAtBefore(
                List.of(PaymentStatus.INITIATED, PaymentStatus.PENDING), now);
        int n = 0;
        for (var p : list) {
            var prev = p.getStatus();
            p.setStatus(PaymentStatus.EXPIRED);
            p.setExpiresAt(null);
            paymentRepo.save(p);
            saveEvent(p, prev, PaymentStatus.EXPIRED, "system", "timeout");

            var o = p.getOrder();
            rollbackStock(o);
            o.setStatus(OrderStatus.CANCELED);
            orderRepo.save(o);
            n++;
        }
        return n;
    }

    // ===== helpers =====

    private void initMercadoPago(Payment p, Order order) {
        if (mpAccessToken == null || mpAccessToken.isBlank())
            throw new IllegalStateException("Falta mp.access-token");
        if (mpWebhookUrl == null || mpWebhookUrl.isBlank())
            throw new IllegalStateException("Falta mp.webhook-url");

        var title  = "Orden " + order.getId();
        var qty    = 1;
        var amount = order.getTotalAmount();

        String body = """
    {
      "items":[
        {"title":"%s","quantity":%d,"currency_id":"ARS","unit_price":%s}
      ],
      "payer":{"email":"%s"},
      "back_urls":{
        "success":"%s/checkout/success",
        "failure":"%s/checkout/failure",
        "pending":"%s/checkout/pending"
      },
      "auto_return":"approved",
      "notification_url":"%s",
      "external_reference":"order-%d"
    }
    """.formatted(
                title, qty, amount,
                order.getUser().getEmail(),
                // si querés, sacá estos 3 de props también
                originBase(), originBase(), originBase(),
                mpWebhookUrl, order.getId()
        );

        var req = HttpRequest.newBuilder(URI.create("https://api.mercadopago.com/checkout/preferences"))
                .header("Authorization", "Bearer " + mpAccessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            var res = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() / 100 != 2) {
                throw new IllegalStateException("Error creando preference MP: " + res.body());
            }

            var node = new ObjectMapper().readTree(res.body());
            String initPoint = node.path("init_point").asText(null);
            String prefId    = node.path("id").asText(null);
            if (initPoint == null || prefId == null) {
                throw new IllegalStateException("Respuesta MP inválida: " + res.body());
            }

            p.setProvider("MERCADO_PAGO");
            p.setProviderPaymentId(prefId);
            // guardamos la URL para que el FE pueda redirigir
            p.setProviderMetadata("{\"init_point\":\"" + initPoint + "\"}");
        } catch (IOException e) {
            throw new IllegalStateException("Fallo IO inicializando Mercado Pago", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Operación interrumpida inicializando Mercado Pago", e);
        }
    }

    // si no tenés un "origin" configurado, podés devolver vacío o sacarlo del request en el controller
    private String originBase() { return "https://example.com"; }




    private String extractProviderPaymentId(String provider, Map<String,Object> payload) {
        // TODO: extraer del payload del webhook (MP: "data.id" o "resource")
        Object id = payload.get("id");
        return id != null ? String.valueOf(id) : null;
    }

    private PaymentStatus mapGatewayStatus(String provider, Map<String,Object> payload) {
        // TODO: mapear estados del provider a PaymentStatus
        // MP: "approved" → APPROVED, "rejected" → REJECTED, "in_process"/"pending" → PENDING
        return PaymentStatus.PENDING;
    }

    private Payment mustPayment(Order o, PaymentMethod expected) {
        Payment p = o.getPayment();
        if (p == null) throw new IllegalStateException("La orden no tiene Payment");
        if (p.getMethod() != expected) throw new IllegalStateException("Método de pago inválido");
        return p;
    }

    private boolean isExpired(Payment p) {
        return p.getExpiresAt() != null && p.getExpiresAt().isBefore(LocalDateTime.now());
    }

    private void saveEvent(Payment p, PaymentStatus from, PaymentStatus to, String who, String note) {
        PaymentEvent ev = new PaymentEvent();
        ev.setPayment(p);
        ev.setFromStatus(from);
        ev.setToStatus(to);
        ev.setEventAt(LocalDateTime.now());
        ev.setTriggeredBy(who);
        ev.setNote(note);
        paymentEventRepo.save(ev);
    }

    private void rollbackStock(Order o) {
        if (o.getItems() == null) return;
        o.getItems().forEach(oi -> {
            var v = oi.getVariant();
            if (v != null) {
                v.setStock((v.getStock() == null ? 0 : v.getStock()) + oi.getQuantity());
                variantRepo.save(v);
            }
        });
    }
}
