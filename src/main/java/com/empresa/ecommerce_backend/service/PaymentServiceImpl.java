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
import com.fasterxml.jackson.core.type.TypeReference;
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

    @Value("${front.base-url}")
    private String frontBaseUrl;

    private final ObjectMapper om = new ObjectMapper();

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
    public void handleGatewayWebhook(String provider, Map<String, Object> payload) {
        // 1) sacar payment_id del payload (body o query merged por el controller)
        String paymentId = extractMpPaymentId(payload);
        if (paymentId == null) return;

        // 2) consultar a MP el pago
        MpPayment mp = getMpPayment(paymentId);
        if (mp == null) return;

        // 3) Resolver orden por external_reference = "order-<id>"
        String extRef = mp.external_reference();
        if (extRef == null || !extRef.startsWith("order-")) return;
        Long orderId = Long.valueOf(extRef.substring("order-".length()));

        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));

        Payment p = o.getPayment();
        if (p == null) throw new IllegalStateException("La orden no tiene Payment");

        // Enlazá el payment_id real si aún no lo guardaste
        if (p.getProviderPaymentId() == null) p.setProviderPaymentId(mp.id());

        PaymentStatus newStatus = switch (mp.status()) {
            case "approved" -> PaymentStatus.APPROVED;
            case "rejected" -> PaymentStatus.REJECTED;
            case "cancelled", "refunded", "charged_back" -> PaymentStatus.CANCELED;
            case "pending", "in_process" -> PaymentStatus.PENDING;
            default -> PaymentStatus.PENDING;
        };

        if (p.getStatus() != newStatus) {
            var prev = p.getStatus();
            p.setStatus(newStatus);
            p.setExpiresAt(null);
            paymentRepo.save(p);
            saveEvent(p, prev, newStatus, "webhook:MERCADO_PAGO", null);

            switch (newStatus) {
                case APPROVED -> { o.setStatus(OrderStatus.PAID);     orderRepo.save(o); }
                case REJECTED, CANCELED, EXPIRED -> {
                    rollbackStock(o);
                    o.setStatus(OrderStatus.CANCELED);               orderRepo.save(o);
                }
                default -> { /* pending: no tocar orden */ }
            }
        }
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
        p.setExpiresAt(LocalDateTime.now().plusHours(48));
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
            return ServiceResult.ok(orderMapper.toResponse(o));
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
            return ServiceResult.ok(orderMapper.toResponse(o));
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

    // =================== MP helpers ===================

    private void initMercadoPago(Payment p, Order order) {
        if (mpAccessToken == null || mpAccessToken.isBlank())
            throw new IllegalStateException("Falta mp.access-token");
        if (mpWebhookUrl == null || mpWebhookUrl.isBlank())
            throw new IllegalStateException("Falta mp.webhook-url");
        if (frontBaseUrl == null || frontBaseUrl.isBlank())
            throw new IllegalStateException("Falta front.base-url");

        String base = frontBaseUrl.trim().replaceAll("/+$", "");
        if (!(base.startsWith("http://") || base.startsWith("https://"))) {
            throw new IllegalStateException("front.base-url debe empezar con http:// o https://");
        }

        String success = base + "/checkout/success";
        String failure = base + "/checkout/failure";
        String pending = base + "/checkout/pending";

        var om = new com.fasterxml.jackson.databind.ObjectMapper();

        Map<String,Object> item = Map.of(
                "title", "Orden " + order.getId(),
                "quantity", 1,
                "currency_id", "ARS",
                "unit_price", order.getTotalAmount()
        );
        Map<String,Object> pref = new java.util.LinkedHashMap<>();
        pref.put("items", java.util.List.of(item));
        pref.put("payer", Map.of("email", order.getUser().getEmail()));
        pref.put("back_urls", Map.of("success", success, "failure", failure, "pending", pending));
        pref.put("notification_url", mpWebhookUrl);
        pref.put("external_reference", "order-" + order.getId());
        pref.put("binary_mode", true);

        // Solo enviar auto_return si la base es HTTPS (evita el invalid_auto_return en dev)
        if (base.startsWith("https://")) {
            pref.put("auto_return", "approved");
        }

        String body;
        try {
            body = om.writeValueAsString(pref);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo serializar la preferencia", e);
        }

        var req = java.net.http.HttpRequest.newBuilder(java.net.URI.create("https://api.mercadopago.com/checkout/preferences"))
                .header("Authorization", "Bearer " + mpAccessToken)
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            var res = java.net.http.HttpClient.newHttpClient().send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() / 100 != 2) {
                System.err.println("[MP preference body] " + body); // 👈 imprime lo que MP recibió
                throw new IllegalStateException("Error creando preference MP: " + res.body());
            }
            var node = om.readTree(res.body());
            String initPoint = node.path("init_point").asText(null);
            String prefId    = node.path("id").asText(null);
            if (initPoint == null || prefId == null) {
                throw new IllegalStateException("Respuesta MP inválida: " + res.body());
            }

            p.setProvider("MERCADO_PAGO");
            p.setProviderPreferenceId(prefId);
            p.setProviderPaymentId(null);
            p.setProviderMetadata(om.writeValueAsString(Map.of(
                    "init_point", initPoint,
                    "preference_id", prefId
            )));
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Fallo IO inicializando Mercado Pago", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Operación interrumpida inicializando Mercado Pago", e);
        }
    }


    private String extractMpPaymentId(Map<String,Object> payload) {
        // body: { "type":"payment", "data":{"id":"123"} } ó { "action":"payment.created", "data":{"id":"123"} }
        Object data = payload.get("data");
        if (data instanceof Map<?,?> m) {
            Object id = m.get("id");
            if (id != null) return String.valueOf(id);
        }
        // query mergeada por el controller: ?topic=payment&id=123
        Object topic = payload.get("topic");
        Object id = payload.get("id");
        if (id != null && "payment".equalsIgnoreCase(String.valueOf(topic))) {
            return String.valueOf(id);
        }
        // a veces: "resource": ".../v1/payments/{id}"
        Object resource = payload.get("resource");
        if (resource != null) {
            String r = String.valueOf(resource);
            int i = r.lastIndexOf('/');
            if (i > -1) return r.substring(i + 1);
        }
        return null;
    }

    private MpPayment getMpPayment(String paymentId) {
        try {
            var req = HttpRequest.newBuilder(URI.create("https://api.mercadopago.com/v1/payments/" + paymentId))
                    .header("Authorization", "Bearer " + mpAccessToken)
                    .GET()
                    .build();
            var res = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() / 100 != 2) return null;

            var json = om.readTree(res.body());
            return new MpPayment(
                    json.path("id").asText(),
                    json.path("status").asText(),
                    json.path("external_reference").asText(null)
            );
        } catch (Exception e) {
            return null;
        }
    }

    private record MpPayment(String id, String status, String external_reference) {}

    // ===== comunes =====

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
