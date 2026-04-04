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
import com.empresa.ecommerce_backend.model.OrderItem;
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
import com.empresa.ecommerce_backend.repository.ProductRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final MetaPixelService metaPixelService;

    private final UserRepository userRepo; // Inject UserRepository
    private final OrderRepository orderRepo;
    private final PaymentRepository paymentRepo;
    private final PaymentEventRepository paymentEventRepo;
    private final ProductVariantRepository variantRepo;
    private final OrderMapper orderMapper;
    private final ProductRepository productRepo;
    private final com.empresa.ecommerce_backend.service.interfaces.EmailService emailService;

    @Value("${mp.access-token}")
    private String mpAccessToken;

    @Value("${mp.webhook-url}")
    private String mpWebhookUrl;

    @Value("${front.base-url}")
    private String frontBaseUrl;

    @Value("${mp.webhook-secret:}")
    private String mpWebhookSecret;

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
        LocalDateTime expiration = switch (method) {
            case BANK_TRANSFER -> LocalDateTime.now().plusHours(2); // 2 horas para hacer la transferencia
            case CASH -> LocalDateTime.now().plusHours(48);
            default -> LocalDateTime.now().plusMinutes(30); // MercadoPago, Card, etc.
        };
        p.setExpiresAt(expiration);

        // 👇 Capture User Context for Meta (IP, UA, Cookies)
        try {
            var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                p.setClientIp(metaPixelService.extractClientIp(req));
                p.setUserAgent(req.getHeader("User-Agent"));

                var cookies = metaPixelService.extractFbpFbc(req);
                p.setFbp(cookies.get(0));
                p.setFbc(cookies.get(1));
            }
        } catch (Exception e) {
            // Context retrieval failed (e.g. called from scheduled task), ignore.
        }

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

        // 📧 Notificar nueva orden
        emailService.sendOrderConfirmation(o.getId());

        return ServiceResult.ok(orderMapper.toResponse(o));
    }

    @Override
    @Transactional
    public Order handleGatewayWebhook(String provider, Map<String, Object> payload, Map<String, String> headers) {
        // Validación de firma webhook de Mercado Pago
        if ("MERCADO_PAGO".equals(provider) && mpWebhookSecret != null && !mpWebhookSecret.isBlank()) {
            boolean isValid = validateMercadoPagoSignature(payload, headers);
            if (!isValid) {
                System.err.println("❌ Firma MP inválida. Rechazando webhook.");
                return null;
            }
        }

        // 1) sacar payment_id del payload (body o query merged por el controller)
        String paymentId = extractMpPaymentId(payload);
        if (paymentId == null)
            return null;

        // 2) consultar a MP el pago
        MpPayment mp = getMpPayment(paymentId);
        if (mp == null)
            return null;

        // 3) Resolver orden por external_reference = "order-<id>"
        String extRef = mp.external_reference();
        if (extRef == null || !extRef.startsWith("order-"))
            return null;
        Long orderId = Long.valueOf(extRef.substring("order-".length()));

        Order o = orderRepo.findByIdWithLock(orderId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));

        Payment p = o.getPayment();
        if (p == null)
            throw new IllegalStateException("La orden no tiene Payment");

        // ✅ Validación de Método de Pago: Evitar que MP apruebe órdenes hechas por Transferencia
        if (p.getMethod() != PaymentMethod.MERCADO_PAGO && p.getMethod() != PaymentMethod.CARD) {
            System.err.println("❌ Webhook MP intentó aprobar una orden con método de pago distinto.");
            return null;
        }

        // Enlazá el payment_id real si aún no lo guardaste
        if (p.getProviderPaymentId() == null)
            p.setProviderPaymentId(mp.id());

        PaymentStatus newStatus = switch (mp.status()) {
            case "approved" -> PaymentStatus.APPROVED;
            case "rejected" -> PaymentStatus.REJECTED;
            case "cancelled", "refunded", "charged_back" -> PaymentStatus.CANCELED;
            case "pending", "in_process" -> PaymentStatus.PENDING;
            default -> PaymentStatus.PENDING;
        };

        // 4) Validar monto (vulnerabilidad de external_reference spoofing)
        if (newStatus == PaymentStatus.APPROVED) {
            java.math.BigDecimal oTotal = o.getTotalAmount();
            java.math.BigDecimal mpTotal = mp.transaction_amount();
            if (mpTotal == null || mpTotal.compareTo(oTotal) != 0) {
                System.err.println("❌ Monto de MP (" + mpTotal + ") no coincide con orden (" + oTotal + ")");
                newStatus = PaymentStatus.PENDING; // No aprobamos
            }
        }

        if (p.getStatus() != newStatus) {
            var prev = p.getStatus();
            p.setStatus(newStatus);
            p.setExpiresAt(null);
            paymentRepo.save(p);
            saveEvent(p, prev, newStatus, "webhook:MERCADO_PAGO", null);

            switch (newStatus) {
                case APPROVED -> {
                    // 👇 Try to link guest order to user if email matches
                    tryLinkOrderToUser(o);

                    // 👇 Marcar orden como pagada + timestamp y registrar ventas
                    if (o.getPaidAt() == null) {
                        o.setPaidAt(LocalDateTime.now());
                        registerSale(o); // 👈 Solo registra si no estaba ya pagada
                    }
                    o.setStatus(OrderStatus.PAID);
                    orderRepo.save(o);

                    // 📧 Notificar pago aprobado
                    emailService.sendPaymentApprovedNotification(o.getId());

                    // 📊 EVENTO PURCHASE (Meta)
                    // Usar datos guardados en Payment p para "impersonar" al usuario original
                    String userIp = (p.getClientIp() != null) ? p.getClientIp() : "0.0.0.0";
                    String userAgent = (p.getUserAgent() != null) ? p.getUserAgent() : "Backend/Webhook";
                    String fbp = p.getFbp();
                    String fbc = p.getFbc();

                    metaPixelService.sendEvent(
                            "Purchase",
                            userIp,
                            userAgent,
                            frontBaseUrl + "/checkout/success", // URL lógica de éxito
                            java.util.Arrays.asList(fbp, fbc),
                            o.getUser(),
                            o.getTotalAmount().doubleValue(),
                            "ARS",
                            "order-" + o.getOrderNumber());

                    return o;
                }
                case REJECTED, CANCELED, EXPIRED -> {
                    rollbackStockAndSales(o);
                    o.setStatus(OrderStatus.CANCELED);
                    orderRepo.save(o);
                }
                default -> {
                    /* pending: no tocar orden */ }
            }
        }
        return null;
    }

    @Override
    @Transactional
    public ServiceResult<OrderResponse> confirmBankTransferByOrderNumber(String orderNumber, String reference,
            String receiptUrl) {
        Order o = orderRepo.findByOrderNumber(orderNumber)
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
        p.setExpiresAt(LocalDateTime.now().plusHours(48)); // 48 horas para que admin revise
        paymentRepo.save(p);

        if (o.getStatus() == OrderStatus.PENDING) {
            o.setStatus(OrderStatus.CONFIRMED);
            orderRepo.save(o);
        }

        saveEvent(p, prev, PaymentStatus.PENDING, "user/guest", "transfer confirmed via orderNumber");

        // 📧 Notificar al admin que hay una transferencia para revisar
        emailService.sendTransferPendingAdminNotification(o.getId(), p.getId());

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
            // 👇 Try to link guest order to user if email matches
            tryLinkOrderToUser(o);

            // 👇 Marcar orden como pagada + timestamp y registrar ventas
            if (o.getPaidAt() == null) {
                o.setPaidAt(LocalDateTime.now());
                registerSale(o);
            }
            o.setStatus(OrderStatus.PAID);

            // 📧 Notificar pago aprobado
            emailService.sendPaymentApprovedNotification(o.getId());

            // 📊 EVENTO PURCHASE (Meta)
            String userIp = (p.getClientIp() != null) ? p.getClientIp() : "0.0.0.0";
            String userAgent = (p.getUserAgent() != null) ? p.getUserAgent() : "Backend/AdminAction";
            String fbp = p.getFbp();
            String fbc = p.getFbc();

            metaPixelService.sendEvent(
                    "Purchase",
                    userIp,
                    userAgent,
                    frontBaseUrl + "/checkout/success",
                    java.util.Arrays.asList(fbp, fbc),
                    o.getUser(),
                    o.getTotalAmount().doubleValue(),
                    "ARS",
                    "order-" + o.getOrderNumber());

        } else {
            rollbackStockAndSales(o);
            o.setStatus(OrderStatus.CANCELED);
        }
        orderRepo.save(o);

        saveEvent(p, prev, p.getStatus(), "admin", note);
        return ServiceResult.ok(orderMapper.toResponse(o));
    }

    private void registerSale(Order o) {
        if (o.getItems() == null)
            return;

        for (OrderItem oi : o.getItems()) {
            var v = oi.getVariant();
            if (v == null)
                continue;
            var product = v.getProduct();
            int qty = oi.getQuantity();

            // sumar ventas por SKU (variante)
            v.setSoldCount(v.getSoldCount() + qty);
            variantRepo.save(v);

            // sumar ventas por producto base (ranking catálogo)
            if (product != null) {
                product.setSoldCount(product.getSoldCount() + qty);
                productRepo.save(product);
            }
        }
    }

    private void deregisterSale(Order o) {
        if (o.getItems() == null)
            return;

        for (OrderItem oi : o.getItems()) {
            var v = oi.getVariant();
            if (v == null)
                continue;
            var product = v.getProduct();
            int qty = oi.getQuantity();

            // restar ventas por SKU (variante)
            v.setSoldCount(Math.max(0, v.getSoldCount() - qty));
            variantRepo.save(v);

            // restar ventas por producto base
            if (product != null) {
                product.setSoldCount(Math.max(0, product.getSoldCount() - qty));
                productRepo.save(product);
            }
        }
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

        rollbackStockAndSales(o);
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
            rollbackStockAndSales(o);
            o.setStatus(OrderStatus.CANCELED);
            orderRepo.save(o);

            // 📧 Notificar al usuario que su orden expiró
            emailService.sendPaymentExpiredNotification(o.getId());

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

        Map<String, Object> item = Map.of(
                "title", "Orden " + order.getId(),
                "quantity", 1,
                "currency_id", "ARS",
                "unit_price", order.getTotalAmount());
        Map<String, Object> pref = new java.util.LinkedHashMap<>();
        pref.put("items", java.util.List.of(item));

        // Usar guestEmail si es guest, sino email del usuario
        String payerEmail = order.getUser() != null
                ? order.getUser().getEmail()
                : order.getGuestEmail();
        pref.put("payer", Map.of("email", payerEmail));
        pref.put("back_urls", Map.of("success", success, "failure", failure, "pending", pending));
        pref.put("notification_url", mpWebhookUrl);
        pref.put("external_reference", "order-" + order.getId());
        pref.put("binary_mode", true);

        // Solo enviar auto_return si la base es HTTPS (evita el invalid_auto_return en
        // dev)
        if (base.startsWith("https://")) {
            pref.put("auto_return", "approved");
        }

        String body;
        try {
            body = om.writeValueAsString(pref);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo serializar la preferencia", e);
        }

        var req = java.net.http.HttpRequest
                .newBuilder(java.net.URI.create("https://api.mercadopago.com/checkout/preferences"))
                .header("Authorization", "Bearer " + mpAccessToken)
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            var res = java.net.http.HttpClient.newHttpClient().send(req,
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() / 100 != 2) {
                System.err.println("[MP preference body] " + body); // 👈 imprime lo que MP recibió
                throw new IllegalStateException("Error creando preference MP: " + res.body());
            }
            var node = om.readTree(res.body());
            String initPoint = node.path("init_point").asText(null);
            String prefId = node.path("id").asText(null);
            if (initPoint == null || prefId == null) {
                throw new IllegalStateException("Respuesta MP inválida: " + res.body());
            }

            p.setProvider("MERCADO_PAGO");
            p.setProviderPreferenceId(prefId);
            p.setProviderPaymentId(null);
            p.setProviderMetadata(om.writeValueAsString(Map.of(
                    "init_point", initPoint,
                    "preference_id", prefId)));
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Fallo IO inicializando Mercado Pago", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Operación interrumpida inicializando Mercado Pago", e);
        }
    }

    private String extractMpPaymentId(Map<String, Object> payload) {
        // body: { "type":"payment", "data":{"id":"123"} } ó {
        // "action":"payment.created", "data":{"id":"123"} }
        Object data = payload.get("data");
        if (data instanceof Map<?, ?> m) {
            Object id = m.get("id");
            if (id != null)
                return String.valueOf(id);
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
            if (i > -1)
                return r.substring(i + 1);
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
            if (res.statusCode() / 100 != 2)
                return null;

            var json = om.readTree(res.body());
            return new MpPayment(
                    json.path("id").asText(),
                    json.path("status").asText(),
                    json.path("external_reference").asText(null),
                    java.math.BigDecimal.valueOf(json.path("transaction_amount").asDouble(0.0)));
        } catch (Exception e) {
            return null;
        }
    }

    private record MpPayment(String id, String status, String external_reference, java.math.BigDecimal transaction_amount) {
    }

    // ===== comunes =====

    private Payment mustPayment(Order o, PaymentMethod expected) {
        Payment p = o.getPayment();
        if (p == null)
            throw new IllegalStateException("La orden no tiene Payment");
        if (p.getMethod() != expected)
            throw new IllegalStateException("Método de pago inválido");
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

    private void rollbackStockAndSales(Order o) {
        if (o.getItems() == null)
            return;

        // Si la orden ya estaba pagada, tenemos que descontar las ventas registradas
        if (o.getPaidAt() != null) {
            deregisterSale(o);
            o.setPaidAt(null);
            // orderRepo.save(o); // Se asume que el llamador guarda la orden (o.setStatus...)
        }

        o.getItems().forEach(oi -> {
            var v = oi.getVariant();
            if (v != null) {
                // NO devolver stock a productos digitales on-demand (tienen stock ilimitado)
                if (v.getFulfillmentType() != com.empresa.ecommerce_backend.enums.FulfillmentType.DIGITAL_ON_DEMAND) {
                    v.setStock((v.getStock() == null ? 0 : v.getStock()) + oi.getQuantity());
                    variantRepo.save(v);
                }
            }
        });

    }

    /**
     * Intenta vincular una orden de invitado a un usuario existente
     * si el email de la orden coincide con el de un usuario registrado.
     */
    private void tryLinkOrderToUser(Order o) {
        if (o.getUser() == null && o.getGuestEmail() != null) {
            userRepo.findByEmail(o.getGuestEmail()).ifPresent(user -> {
                o.setUser(user);
                // Opcional: limpiar guestEmail o dejarlo como histórico
                // o.setGuestEmail(null);
            });
        }
    }

    private boolean validateMercadoPagoSignature(Map<String, Object> payload, Map<String, String> headers) {
        String xSignature = headers.get("x-signature");
        String xRequestId = headers.get("x-request-id");
        if (xSignature == null || xRequestId == null) return false;

        String ts = null;
        String v1 = null;
        for (String part : xSignature.split(",")) {
            if (part.startsWith("ts=")) ts = part.substring(3);
            else if (part.startsWith("v1=")) v1 = part.substring(3);
        }
        if (ts == null || v1 == null) return false;

        String dataId = extractMpPaymentId(payload);
        if (dataId == null) return false;

        // manifest: id:[data.id];request-id:[x-request-id];ts:[ts];
        String manifest = "id:" + dataId + ";request-id:" + xRequestId + ";ts:" + ts + ";";

        try {
            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(mpWebhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256HMAC.init(secretKey);

            byte[] hashBytes = sha256HMAC.doFinal(manifest.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().equalsIgnoreCase(v1);
        } catch (Exception e) {
            return false;
        }
    }
}
