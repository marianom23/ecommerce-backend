// src/main/java/com/empresa/ecommerce_backend/service/OrderServiceImpl.java
package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.ConfirmOrderRequest;
import com.empresa.ecommerce_backend.dto.request.UpdateBillingProfileRequest;
import com.empresa.ecommerce_backend.dto.request.UpdatePaymentMethodRequest;
import com.empresa.ecommerce_backend.dto.request.UpdateShippingAddressRequest;
import com.empresa.ecommerce_backend.dto.response.OrderResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.dto.response.OrderSummaryResponse;
import com.empresa.ecommerce_backend.dto.response.PageResponse;
import com.empresa.ecommerce_backend.enums.FulfillmentType;
import com.empresa.ecommerce_backend.enums.OrderStatus;
import com.empresa.ecommerce_backend.enums.PaymentMethod;
import com.empresa.ecommerce_backend.enums.PaymentStatus;
import com.empresa.ecommerce_backend.exception.RecursoNoEncontradoException;
import com.empresa.ecommerce_backend.mapper.OrderMapper;
import com.empresa.ecommerce_backend.mapper.SnapshotMapper;
import com.empresa.ecommerce_backend.model.*;
import com.empresa.ecommerce_backend.model.embeddable.BillingSnapshot;
import com.empresa.ecommerce_backend.repository.*;
import com.empresa.ecommerce_backend.repository.projection.OrderSummaryProjection;
import com.empresa.ecommerce_backend.service.interfaces.OrderService;
import com.empresa.ecommerce_backend.service.interfaces.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepo;
    private final AddressRepository addressRepo;
    private final BillingProfileRepository billingRepo;
    private final OrderRepository orderRepo;
    private final ProductVariantRepository variantRepo;
    private final CartRepository cartRepo;
    private final PaymentRepository paymentRepo;
    private final OrderMapper orderMapper;
    private final PaymentService paymentService;

    private Long currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) throw new IllegalArgumentException("No autenticado.");
        try {
            return (Long) auth.getPrincipal().getClass().getMethod("getId").invoke(auth.getPrincipal());
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo obtener ID de usuario autenticado");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<OrderResponse> getOneByNumber(String orderNumber) {
        Long uid = currentUserId();
        Order o = orderRepo.findByOrderNumberAndUserId(orderNumber, uid)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
        return ServiceResult.ok(orderMapper.toResponse(o));
    }

    // ====== Crear SIN body, solo logged-in ======
    @Override
    @Transactional
    public ServiceResult<OrderResponse> createOrder() {
        Long uid = currentUserId();
        User user = userRepo.findById(uid)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        // Carrito del user
        Cart cart = cartRepo.findByUserId(user.getId()).orElse(null);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST, "Tu carrito está vacío.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        // snapshots arrancan null; se llenan con los PATCH
        order.setShippingAddress(null);
        order.setBillingInfo(null);

        // Ítems + reserva de stock + subTotal
        BigDecimal subTotal = BigDecimal.ZERO;
        for (CartItem ci : cart.getItems()) {
            if (ci.getQuantity() == null || ci.getQuantity() <= 0) continue;

            ProductVariant v = ci.getVariant();
            if (v == null) {
                return ServiceResult.error(HttpStatus.BAD_REQUEST, "Ítem inválido en el carrito (falta variante).");
            }

            int qty = ci.getQuantity();

            // ⬇️ Para FÍSICOS y DIGITALES INSTANT: validar y reservar stock
            if (!isDigitalOnDemand(v)) {
                int stock = (v.getStock() == null ? 0 : v.getStock());
                if (qty > stock) {
                    return ServiceResult.error(HttpStatus.CONFLICT, "Stock insuficiente para SKU " + v.getSku());
                }
            }

            BigDecimal unitPrice = v.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setVariant(v);
            oi.setProductName(v.getProduct().getName());
            oi.setSku(v.getSku());
            oi.setAttributesJson(v.getAttributesJson());
            oi.setUnitPrice(unitPrice);
            oi.setQuantity(qty);
            oi.setDiscountAmount(BigDecimal.ZERO);
            oi.setLineTotal(lineTotal);

            // ⬇️ Reservar stock solo si NO es on-demand
            if (!isDigitalOnDemand(v)) {
                int stock = (v.getStock() == null ? 0 : v.getStock());
                v.setStock(stock - qty);
            } else {
                // Si tenés campos en OrderItem para digitales, podés inicializarlos acá:
                // oi.setDigitalDelivered(false);
                // oi.setLicenseKey(null);
            }

            order.getItems().add(oi);
            subTotal = subTotal.add(lineTotal);
        }

        // Montos iniciales (envío y tax en 0 hasta que haya datos)
        order.setSubTotal(subTotal);
        order.setShippingCost(BigDecimal.ZERO);
        order.setTaxAmount(BigDecimal.ZERO);
        order.setDiscountTotal(BigDecimal.ZERO);
        order.setTotalAmount(subTotal); // subtotal + 0 + 0 - 0

        // ⏳ Expiración inicial genérica (hasta que elija método de pago)
        order.setExpiresAt(LocalDateTime.now().plusMinutes(30));

        Order saved = orderRepo.save(order);
        return ServiceResult.created(orderMapper.toResponse(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<OrderResponse> getOne(Long id) {
        Long uid = currentUserId();
        Order o = orderRepo.findByIdAndUserId(id, uid)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
        return ServiceResult.ok(orderMapper.toResponse(o));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<PageResponse<OrderSummaryResponse>> listMineSummaries(Pageable pageable) {
        Long uid = currentUserId();

        // 👇 ahora trae solo órdenes cuyo status != PENDING
        Page<OrderSummaryProjection> page =
                orderRepo.findSummariesByUserIdExcludingStatus(uid, OrderStatus.PENDING, pageable);

        Page<OrderSummaryResponse> mapped = page.map(orderMapper::toSummary);
        return ServiceResult.ok(PageResponse.of(mapped));
    }



    @Override
    @Transactional(readOnly = true)
    public ServiceResult<List<OrderResponse>> listMine() {
        Long uid = currentUserId();
        var list = orderRepo
                .findAllByUserIdAndStatusNotOrderByCreatedAtDesc(uid, OrderStatus.PENDING) // 👈 sin pendientes
                .stream()
                .map(orderMapper::toResponse)
                .toList();
        return ServiceResult.ok(list);
    }


    // ====== PATCH Shipping ======
    @Override
    @Transactional
    public ServiceResult<OrderResponse> patchShippingAddress(Long orderId, UpdateShippingAddressRequest req) {
        Long uid = currentUserId();
        Order o = orderRepo.findByIdAndUserId(orderId, uid)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));

        if (o.getPayment() != null && o.getPayment().getStatus() != PaymentStatus.CANCELED) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST,
                    "La orden ya tiene un pago iniciado. No se puede modificar.");
        }

        if (o.getStatus() != OrderStatus.PENDING) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST, "Solo puede modificar shipping mientras la orden está PENDING.");
        }

        Address shipAddr = addressRepo.findById(req.getShippingAddressId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Dirección de envío no encontrada"));

        var snapshot = SnapshotMapper.toSnapshot(shipAddr);
        // completar opcionales si los mandan
        if (req.getRecipientName() != null) snapshot.setRecipientName(req.getRecipientName());
        if (req.getPhone() != null) snapshot.setPhone(req.getPhone());

        o.setShippingAddress(snapshot);

        // Recalcular costos (envío y eventualmente impuestos)
        recalcTotals(o);

        Order saved = orderRepo.save(o);
        return ServiceResult.ok(orderMapper.toResponse(saved));
    }

    @Override
    @Transactional
    public ServiceResult<OrderResponse> confirmOrder(Long orderId, ConfirmOrderRequest req) {
        Long uid = currentUserId();
        Order o = orderRepo.findByIdAndUserId(orderId, uid)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));

        if (o.getStatus() != OrderStatus.PENDING) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST, "Solo se puede confirmar una orden en estado PENDING.");
        }
        if (o.getPayment() != null && o.getPayment().getStatus() != PaymentStatus.CANCELED) {
            return ServiceResult.ok(orderMapper.toResponse(o)); // idempotencia
        }
        if (o.getItems() == null || o.getItems().isEmpty()) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST, "La orden no tiene ítems.");
        }
        if (requiresShipping(o) && o.getShippingAddress() == null) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST, "Falta la dirección de envío.");
        }
        if (o.getBillingInfo() == null) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST, "Falta la información de facturación.");
        }
        if (o.getChosenPaymentMethod() == null) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST, "Falta el método de pago.");
        }

        recalcTotals(o);

        // 👇 Toda la inicialización de pago (transferencia/MP) vive en PaymentService
        return paymentService.initPaymentForOrder(o, o.getChosenPaymentMethod());
    }


    // ====== PATCH Billing ======
    @Override
    @Transactional
    public ServiceResult<OrderResponse> patchBillingProfile(Long orderId, UpdateBillingProfileRequest req) {

        Long uid = currentUserId();
        Order o = orderRepo.findByIdAndUserId(orderId, uid)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));

        if (o.getPayment() != null && o.getPayment().getStatus() != PaymentStatus.CANCELED) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST,
                    "La orden ya tiene un pago iniciado. No se puede modificar.");
        }

        if (o.getStatus() != OrderStatus.PENDING) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST, "Solo puede modificar facturación mientras la orden está PENDING.");
        }

        BillingProfile bp = billingRepo.findById(req.getBillingProfileId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Perfil de facturación no encontrado"));

        Address billingAddr = bp.getBillingAddress();
        if (billingAddr == null) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST, "El perfil de facturación no tiene dirección asociada");
        }

        o.setBillingInfo(SnapshotMapper.toSnapshot(bp, billingAddr));

        // Si tu cálculo de impuestos depende de billing, recalcular:
        recalcTotals(o);

        Order saved = orderRepo.save(o);
        return ServiceResult.ok(orderMapper.toResponse(saved));
    }

    // ====== PATCH Payment ======
    @Override
    @Transactional
    public ServiceResult<OrderResponse> patchPaymentMethod(Long orderId, UpdatePaymentMethodRequest req) {
        Long uid = currentUserId();
        Order o = orderRepo.findByIdAndUserId(orderId, uid)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));

        if (o.getPayment() != null && o.getPayment().getStatus() != PaymentStatus.CANCELED) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST,
                    "La orden ya tiene un pago iniciado. No se puede modificar.");
        }

        if (o.getStatus() != OrderStatus.PENDING) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST, "Solo puede cambiar el método de pago mientras la orden está PENDING.");
        }

        PaymentMethod method = req.getPaymentMethod();
        o.setChosenPaymentMethod(method);

        // ⏳ Ajustar expiración según el método de pago elegido
        o.setExpiresAt(calculateExpirationFor(method));

        Order saved = orderRepo.save(o);
        return ServiceResult.ok(orderMapper.toResponse(saved));
    }

    // ===== Helpers =====

    private void recalcTotals(Order o) {
        BigDecimal subTotal = o.getItems().stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingCost = requiresShipping(o) && o.getShippingAddress() != null
                ? calcularEnvio(o.getShippingAddress())
                : BigDecimal.ZERO;

        BigDecimal taxAmount = calcularImpuestos(subTotal, o.getBillingInfo());
        BigDecimal discountTotal = o.getDiscountTotal() != null ? o.getDiscountTotal() : BigDecimal.ZERO;

        BigDecimal total = subTotal.subtract(discountTotal).add(shippingCost).add(taxAmount);
        if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;

        o.setSubTotal(subTotal);
        o.setShippingCost(shippingCost);
        o.setTaxAmount(taxAmount);
        o.setDiscountTotal(discountTotal);
        o.setTotalAmount(total);
    }

    private BigDecimal calcularEnvio(Object anyShippingSnapshot) {
        // TODO: integrar cotizador real (Andreani/OCA/etc.)
        return new BigDecimal("0.00");
    }

    private BigDecimal calcularImpuestos(BigDecimal subTotal, BillingSnapshot billing) {
        // TODO: calcular IVA según condición impositiva del billing
        return subTotal.multiply(new BigDecimal("0.00"));
    }

    private boolean isDigital(ProductVariant v) {
        return v != null && v.getFulfillmentType() != null &&
                (v.getFulfillmentType() == FulfillmentType.DIGITAL_ON_DEMAND
                        || v.getFulfillmentType() == FulfillmentType.DIGITAL_INSTANT);
    }

    private boolean isDigitalOnDemand(ProductVariant v) {
        return v != null && v.getFulfillmentType() == FulfillmentType.DIGITAL_ON_DEMAND;
    }

    private boolean requiresShipping(Order o) {
        // requiere envío si existe al menos un ítem NO digital
        return o.getItems().stream().anyMatch(oi -> {
            ProductVariant v = oi.getVariant();
            return !isDigital(v);
        });
    }

    // ⏳ Helper para expiración por método de pago
    private LocalDateTime calculateExpirationFor(PaymentMethod method) {

        if (method == null) {
            return LocalDateTime.now().plusMinutes(30);
        }

        return switch (method) {
            case CARD ->
                    LocalDateTime.now().plusHours(1); // pago inmediato
            case MERCADO_PAGO ->
                    LocalDateTime.now().plusMinutes(30); // preferencia MP rápida
            case PAYPAL ->
                    LocalDateTime.now().plusMinutes(45); // un poco más de margen
            case CASH ->
                    LocalDateTime.now().plusHours(24);   // efectivo
            case BANK_TRANSFER ->
                    LocalDateTime.now().plusHours(48);   // transferencia
        };
    }
}
