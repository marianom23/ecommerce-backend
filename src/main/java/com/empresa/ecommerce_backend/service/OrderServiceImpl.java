// src/main/java/com/empresa/ecommerce_backend/service/OrderServiceImpl.java
package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.ConfirmOrderRequest;
import com.empresa.ecommerce_backend.dto.request.CreateOrderRequest;
import com.empresa.ecommerce_backend.dto.request.UpdateBillingProfileRequest;
import com.empresa.ecommerce_backend.dto.request.UpdateOrderStatusRequest;
import com.empresa.ecommerce_backend.dto.request.UpdatePaymentMethodRequest;
import com.empresa.ecommerce_backend.dto.request.UpdateShippingAddressRequest;
import com.empresa.ecommerce_backend.dto.response.OrderResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.dto.response.OrderSummaryResponse;
import com.empresa.ecommerce_backend.dto.response.OrderBackofficeResponse;
import com.empresa.ecommerce_backend.dto.response.PageResponse;
import com.empresa.ecommerce_backend.enums.FulfillmentType;
import com.empresa.ecommerce_backend.enums.OrderStatus;
import com.empresa.ecommerce_backend.enums.PaymentMethod;
import com.empresa.ecommerce_backend.enums.PaymentStatus;
import com.empresa.ecommerce_backend.exception.RecursoNoEncontradoException;
import com.empresa.ecommerce_backend.mapper.OrderMapper;
import com.empresa.ecommerce_backend.mapper.SnapshotMapper;
import com.empresa.ecommerce_backend.model.*;
import com.empresa.ecommerce_backend.model.embeddable.AddressSnapshot;
import com.empresa.ecommerce_backend.model.embeddable.BillingSnapshot;
import com.empresa.ecommerce_backend.repository.*;
import com.empresa.ecommerce_backend.repository.projection.OrderSummaryProjection;
import com.empresa.ecommerce_backend.service.interfaces.OrderService;
import com.empresa.ecommerce_backend.service.interfaces.PaymentService;
import com.empresa.ecommerce_backend.service.interfaces.CartService;
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
    private final CartService cartService;

    private Long currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null)
            return null; // Permitir guests
        try {
            return (Long) auth.getPrincipal().getClass().getMethod("getId").invoke(auth.getPrincipal());
        } catch (Exception e) {
            return null; // Guest checkout
        }
    }

    private boolean isAuthenticated() {
        return currentUserId() != null;
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<OrderResponse> getOneByNumber(String orderNumber) {
        Long uid = currentUserId();
        Order o;
        if (uid != null) {
            o = orderRepo.findByOrderNumberAndUserId(orderNumber, uid)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
        } else {
            // Guest access: permit lookup by secret orderNumber
            o = orderRepo.findByOrderNumber(orderNumber)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
            if (!o.isGuestOrder()) {
                // If order belongs to a registered user, do not expose it to anonymous
                // (Unless we decide that orderNumber is secret enough. For now, let's be strict
                // or loose.
                // Given the UUID nature, it's fairly safe, but let's restricting non-guests is
                // safer).
                return ServiceResult.error(HttpStatus.FORBIDDEN, "No autorizado");
            }
        }
        return ServiceResult.ok(orderMapper.toResponse(o));
    }

    // ====== Crear orden - soporta users autenticados y guests con sessionId ======
    @Override
    @Transactional
    public ServiceResult<OrderResponse> createOrder(CreateOrderRequest req) {
        Long uid = currentUserId();
        User user = null;
        Cart cart = null;

        if (uid != null) {
            // Usuario autenticado
            user = userRepo.findById(uid)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
            cart = cartRepo.findByUserId(user.getId()).orElse(null);
        } else {
            // Guest checkout - usar sessionId del request
            if (req == null || req.getSessionId() == null || req.getSessionId().isBlank()) {
                return ServiceResult.error(HttpStatus.BAD_REQUEST,
                        "Para invitados, se requiere el sessionId del carrito.");
            }
            cart = cartRepo.findBySessionId(req.getSessionId()).orElse(null);
        }

        // Carrito
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
        // Consolidación de ítems para evitar duplicados de variante
        // Map<Long, OrderItem> consolidated = new HashMap<>();
        // PERO necesitamos iterar para reservar stock.
        // Mejor estrategia: iterar, validar stock y acumular en mapa.

        java.util.Map<Long, OrderItem> itemsMap = new java.util.HashMap<>();
        BigDecimal subTotal = BigDecimal.ZERO;

        for (CartItem ci : cart.getItems()) {
            if (ci.getQuantity() == null || ci.getQuantity() <= 0)
                continue;

            ProductVariant v = ci.getVariant();
            if (v == null) {
                return ServiceResult.error(HttpStatus.BAD_REQUEST, "Ítem inválido en el carrito (falta variante).");
            }

            int qty = ci.getQuantity();

            // ⬇️ Validación y reserva de stock
            if (!isDigitalOnDemand(v)) {
                int stock = (v.getStock() == null ? 0 : v.getStock());
                if (qty > stock) {
                    return ServiceResult.error(HttpStatus.CONFLICT, "Stock insuficiente para SKU " + v.getSku());
                }
                // Descontar stock (acumulativo si hay duplicados? No, hay que tener cuidado)
                // Si hay duplicados, el discount se hace dos veces.
                // Mejor consolidar PRIMERO, luego validar/descontar.
            }

            // Consolidar en memoria antes de crear OrderItems
            OrderItem oi = itemsMap.get(v.getId());
            if (oi == null) {
                oi = new OrderItem();
                oi.setOrder(order);
                oi.setVariant(v);
                oi.setProductName(v.getProduct().getName());
                oi.setSku(v.getSku());
                oi.setAttributesJson(v.getAttributesJson());
                oi.setUnitPrice(v.getPrice());
                oi.setQuantity(0);
                oi.setDiscountAmount(BigDecimal.ZERO);
                oi.setLineTotal(BigDecimal.ZERO);
                itemsMap.put(v.getId(), oi);
            }

            // Sumar cantidad y total
            oi.setQuantity(oi.getQuantity() + qty);
            BigDecimal lineTotal = v.getPrice().multiply(BigDecimal.valueOf(qty));
            oi.setLineTotal(oi.getLineTotal().add(lineTotal));

            subTotal = subTotal.add(lineTotal);
        }

        // Ahora agregar a la orden y validar stock final
        for (OrderItem oi : itemsMap.values()) {
            ProductVariant v = oi.getVariant();
            int qty = oi.getQuantity();

            if (!isDigitalOnDemand(v)) {
                int stock = (v.getStock() == null ? 0 : v.getStock());
                if (qty > stock) {
                    // Restaurar lo que hubiéramos descontado? Aún no guardamos nada.
                    return ServiceResult.error(HttpStatus.CONFLICT,
                            "Stock insuficiente para SKU " + v.getSku() + " (Total: " + qty + ")");
                }
                v.setStock(stock - qty);
            }
            order.getItems().add(oi);
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

        // ✅ VACIAR CARRITO tras compra exitosa
        // (Usamos cartService para asegurar lógica centralizada)
        cartService.clear(uid, req.getSessionId());

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
        Page<OrderSummaryProjection> page = orderRepo.findSummariesByUserIdExcludingStatus(uid, OrderStatus.PENDING,
                pageable);

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
    public ServiceResult<OrderResponse> patchShippingAddress(String orderNumber, UpdateShippingAddressRequest req) {
        Long uid = currentUserId();
        Order o;

        if (uid != null) {
            o = orderRepo.findByOrderNumberAndUserId(orderNumber, uid)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
        } else {
            o = orderRepo.findByOrderNumber(orderNumber)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
            if (!o.isGuestOrder()) {
                return ServiceResult.error(HttpStatus.FORBIDDEN, "No autorizado");
            }
        }

        if (o.getPayment() != null && o.getPayment().getStatus() != PaymentStatus.CANCELED) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST,
                    "La orden ya tiene un pago iniciado. No se puede modificar.");
        }

        if (o.getStatus() != OrderStatus.PENDING) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST,
                    "Solo puede modificar shipping mientras la orden está PENDING.");
        }

        AddressSnapshot snapshot;
        if (isAuthenticated() && req.getShippingAddressId() != null) {
            // Usuario autenticado: obtener de BD
            Address shipAddr = addressRepo.findById(req.getShippingAddressId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Dirección de envío no encontrada"));
            snapshot = SnapshotMapper.toSnapshot(shipAddr);
            if (req.getRecipientName() != null)
                snapshot.setRecipientName(req.getRecipientName());
            if (req.getPhone() != null)
                snapshot.setPhone(req.getPhone());
        } else {
            // Guest: crear snapshot desde request
            snapshot = SnapshotMapper.fromRequest(req);
        }

        o.setShippingAddress(snapshot);

        // Recalcular costos (envío y eventualmente impuestos)
        recalcTotals(o);

        Order saved = orderRepo.save(o);
        return ServiceResult.ok(orderMapper.toResponse(saved));
    }

    @Override
    @Transactional
    public ServiceResult<OrderResponse> confirmOrder(String orderNumber, ConfirmOrderRequest req) {
        Long uid = currentUserId();
        Order o;
        if (uid != null) {
            o = orderRepo.findByOrderNumberAndUserId(orderNumber, uid)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
        } else {
            o = orderRepo.findByOrderNumber(orderNumber)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
            // Permitir confirmar si es guest
            if (!o.isGuestOrder()) {
                return ServiceResult.error(HttpStatus.FORBIDDEN, "No autorizado");
            }
        }

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

        // Validación flexible de billing según tipo de orden
        if (o.getBillingInfo() == null) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST, "Falta la información de facturación.");
        }

        // Para guests con solo productos digitales: validar solo datos mínimos
        if (o.isGuestOrder() && !requiresShipping(o)) {
            var billing = o.getBillingInfo();
            if (billing.getFullName() == null || billing.getEmailForInvoices() == null) {
                return ServiceResult.error(HttpStatus.BAD_REQUEST,
                        "Para productos digitales se requiere nombre completo y email.");
            }
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
    public ServiceResult<OrderResponse> patchBillingProfile(String orderNumber, UpdateBillingProfileRequest req) {

        Long uid = currentUserId();
        Order o;

        if (uid != null) {
            o = orderRepo.findByOrderNumberAndUserId(orderNumber, uid)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
        } else {
            o = orderRepo.findByOrderNumber(orderNumber)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
            if (!o.isGuestOrder()) {
                return ServiceResult.error(HttpStatus.FORBIDDEN, "No autorizado");
            }
        }

        if (o.getPayment() != null && o.getPayment().getStatus() != PaymentStatus.CANCELED) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST,
                    "La orden ya tiene un pago iniciado. No se puede modificar.");
        }

        if (o.getStatus() != OrderStatus.PENDING) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST,
                    "Solo puede modificar facturación mientras la orden está PENDING.");
        }

        BillingSnapshot billingSnapshot;
        if (isAuthenticated() && req.getBillingProfileId() != null) {
            // Usuario autenticado: obtener de BD
            BillingProfile bp = billingRepo.findById(req.getBillingProfileId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Perfil de facturación no encontrado"));
            Address billingAddr = bp.getBillingAddress();
            if (billingAddr == null) {
                return ServiceResult.error(HttpStatus.BAD_REQUEST,
                        "El perfil de facturación no tiene dirección asociada");
            }
            billingSnapshot = SnapshotMapper.toSnapshot(bp, billingAddr);
        } else {
            // Guest: crear snapshot desde request
            billingSnapshot = SnapshotMapper.fromRequest(req);
        }

        o.setBillingInfo(billingSnapshot);

        // Capturar email del guest si no está seteado
        if (o.isGuestOrder() && o.getGuestEmail() == null && billingSnapshot != null) {
            o.setGuestEmail(billingSnapshot.getEmailForInvoices());
        }

        // Si tu cálculo de impuestos depende de billing, recalcular:
        recalcTotals(o);

        Order saved = orderRepo.save(o);
        return ServiceResult.ok(orderMapper.toResponse(saved));
    }

    // ====== PATCH Payment ======
    @Override
    @Transactional
    public ServiceResult<OrderResponse> patchPaymentMethod(String orderNumber, UpdatePaymentMethodRequest req) {
        Long uid = currentUserId();
        Order o;

        if (uid != null) {
            o = orderRepo.findByOrderNumberAndUserId(orderNumber, uid)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
        } else {
            o = orderRepo.findByOrderNumber(orderNumber)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
            if (!o.isGuestOrder()) {
                return ServiceResult.error(HttpStatus.FORBIDDEN, "No autorizado");
            }
        }

        if (o.getPayment() != null && o.getPayment().getStatus() != PaymentStatus.CANCELED) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST,
                    "La orden ya tiene un pago iniciado. No se puede modificar.");
        }

        if (o.getStatus() != OrderStatus.PENDING) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST,
                    "Solo puede cambiar el método de pago mientras la orden está PENDING.");
        }

        PaymentMethod method = req.getPaymentMethod();
        o.setChosenPaymentMethod(method);

        // ⏳ Ajustar expiración según el método de pago elegido
        o.setExpiresAt(calculateExpirationFor(method));

        // Recalcular totales (por si aplica descuento de transferencia)
        recalcTotals(o);

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

        // Descuento por método de pago (Transferencia = 10% off)
        BigDecimal discountTotal = BigDecimal.ZERO;
        if (o.getChosenPaymentMethod() == PaymentMethod.BANK_TRANSFER) {
            discountTotal = subTotal.multiply(new BigDecimal("0.10"));
        }

        // Si hubiera otros descuentos (cupones), se sumarían aquí
        // discountTotal = discountTotal.add(couponDiscount);

        BigDecimal total = subTotal.subtract(discountTotal).add(shippingCost).add(taxAmount);
        if (total.compareTo(BigDecimal.ZERO) < 0)
            total = BigDecimal.ZERO;

        o.setSubTotal(subTotal);
        o.setShippingCost(shippingCost);
        o.setTaxAmount(taxAmount);
        o.setDiscountTotal(discountTotal.setScale(2, java.math.RoundingMode.HALF_UP));
        o.setTotalAmount(total.setScale(2, java.math.RoundingMode.HALF_UP));
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

    // ====== PATCH Order Status (SHIPPED/DELIVERED) ======
    @Override
    @Transactional
    public ServiceResult<OrderResponse> updateOrderStatus(Long orderId, UpdateOrderStatusRequest req) {
        Long uid = currentUserId();
        Order o = orderRepo.findByIdAndUserId(orderId, uid)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));

        OrderStatus newStatus = req.getStatus();

        // Validar que solo se puedan actualizar a SHIPPED o DELIVERED
        if (newStatus != OrderStatus.SHIPPED && newStatus != OrderStatus.DELIVERED) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST,
                    "Solo se puede actualizar el estado a SHIPPED o DELIVERED.");
        }

        // Validar transiciones de estado
        OrderStatus currentStatus = o.getStatus();

        if (newStatus == OrderStatus.SHIPPED) {
            // Para marcar como SHIPPED, debe estar en PAID
            if (currentStatus != OrderStatus.PAID) {
                return ServiceResult.error(HttpStatus.BAD_REQUEST,
                        "Solo se puede marcar como SHIPPED una orden que esté PAID.");
            }
            // No permitir SHIPPED si la orden es 100% digital
            if (!requiresShipping(o)) {
                return ServiceResult.error(HttpStatus.BAD_REQUEST,
                        "No se puede marcar como SHIPPED una orden completamente digital.");
            }
        } else if (newStatus == OrderStatus.DELIVERED) {
            // Para marcar como DELIVERED:
            // - Si requiere envío físico: debe estar en SHIPPED
            // - Si es 100% digital: puede pasar directo de PAID
            boolean isDigitalOnly = !requiresShipping(o);

            if (isDigitalOnly) {
                // Para órdenes digitales: PAID -> DELIVERED
                if (currentStatus != OrderStatus.PAID && currentStatus != OrderStatus.SHIPPED) {
                    return ServiceResult.error(HttpStatus.BAD_REQUEST,
                            "Solo se puede marcar como DELIVERED una orden digital que esté PAID o SHIPPED.");
                }
            } else {
                // Para órdenes físicas: debe estar SHIPPED
                if (currentStatus != OrderStatus.SHIPPED) {
                    return ServiceResult.error(HttpStatus.BAD_REQUEST,
                            "Solo se puede marcar como DELIVERED una orden física que esté SHIPPED.");
                }
            }
        }

        o.setStatus(newStatus);
        Order saved = orderRepo.save(o);
        return ServiceResult.ok(orderMapper.toResponse(saved));
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
                LocalDateTime.now().plusHours(24); // efectivo
            case BANK_TRANSFER ->
                LocalDateTime.now().plusHours(48); // transferencia
        };
    }

    // ====== Admin Backoffice: lista todas las órdenes ======
    @Override
    @Transactional(readOnly = true)
    public ServiceResult<PageResponse<OrderBackofficeResponse>> listAllOrdersForBackoffice(
            Pageable pageable, String search, com.empresa.ecommerce_backend.enums.OrderStatus orderStatus,
            com.empresa.ecommerce_backend.enums.PaymentStatus paymentStatus) {

        // Construir especificaciones dinámicamente
        org.springframework.data.jpa.domain.Specification<Order> spec = org.springframework.data.jpa.domain.Specification
                .where(null);

        // Filtro por búsqueda (order number o email usuario)
        if (search != null && !search.isBlank()) {
            String searchLower = search.toLowerCase().trim();
            spec = spec.and((root, query, cb) -> {
                jakarta.persistence.criteria.Join<Order, User> userJoin = root.join("user");
                return cb.or(
                        cb.like(cb.lower(root.get("orderNumber")), "%" + searchLower + "%"),
                        cb.like(cb.lower(userJoin.get("email")), "%" + searchLower + "%"));
            });
        }

        // Filtro por estado de orden
        if (orderStatus != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), orderStatus));
        }

        // Filtro por estado de pago
        if (paymentStatus != null) {
            spec = spec.and((root, query, cb) -> {
                jakarta.persistence.criteria.Join<Order, Payment> paymentJoin = root.join("payment",
                        jakarta.persistence.criteria.JoinType.LEFT);
                return cb.equal(paymentJoin.get("status"), paymentStatus);
            });
        }

        // Ejecutar query
        org.springframework.data.domain.Page<Order> page = orderRepo.findAll(spec, pageable);

        // Mapear a DTO
        org.springframework.data.domain.Page<OrderBackofficeResponse> mapped = page.map(o -> {
            OrderBackofficeResponse dto = new OrderBackofficeResponse();
            dto.setId(o.getId());
            dto.setOrderNumber(o.getOrderNumber());
            dto.setUserEmail(o.getUser() != null ? o.getUser().getEmail() : null);
            dto.setOrderDate(o.getOrderDate());
            dto.setTotalAmount(o.getTotalAmount());
            dto.setOrderStatus(o.getStatus());

            // Extraer payment info
            if (o.getPayment() != null) {
                dto.setPaymentStatus(o.getPayment().getStatus());
                dto.setPaymentMethod(o.getPayment().getMethod() != null ? o.getPayment().getMethod().name() : null);
            }

            return dto;
        });

        return ServiceResult.ok(PageResponse.of(mapped));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<OrderResponse> getOrderByIdForAdmin(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
        return ServiceResult.ok(orderMapper.toResponse(order));
    }

    @Override
    @Transactional
    public ServiceResult<OrderResponse> updateOrderStatusForAdmin(Long orderId, UpdateOrderStatusRequest req) {
        // Admin version: NO valida userId
        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));

        OrderStatus newStatus = req.getStatus();

        // Validar que solo se puedan actualizar a SHIPPED o DELIVERED
        if (newStatus != OrderStatus.SHIPPED && newStatus != OrderStatus.DELIVERED) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST,
                    "Solo se puede actualizar el estado a SHIPPED o DELIVERED.");
        }

        // Validar transiciones de estado
        OrderStatus currentStatus = o.getStatus();

        if (newStatus == OrderStatus.SHIPPED) {
            // Para marcar como SHIPPED, debe estar en PAID
            if (currentStatus != OrderStatus.PAID) {
                return ServiceResult.error(HttpStatus.BAD_REQUEST,
                        "Solo se puede marcar como SHIPPED una orden que esté PAID.");
            }
            // No permitir SHIPPED si la orden es 100% digital
            if (!requiresShipping(o)) {
                return ServiceResult.error(HttpStatus.BAD_REQUEST,
                        "No se puede marcar como SHIPPED una orden completamente digital.");
            }
        } else if (newStatus == OrderStatus.DELIVERED) {
            // Para marcar como DELIVERED:
            // - Si requiere envío físico: debe estar en SHIPPED
            // - Si es 100% digital: puede pasar directo de PAID
            boolean isDigitalOnly = !requiresShipping(o);

            if (isDigitalOnly) {
                // Para órdenes digitales: PAID -> DELIVERED
                if (currentStatus != OrderStatus.PAID && currentStatus != OrderStatus.SHIPPED) {
                    return ServiceResult.error(HttpStatus.BAD_REQUEST,
                            "Solo se puede marcar como DELIVERED una orden digital que esté PAID o SHIPPED.");
                }
            } else {
                // Para órdenes físicas: debe estar SHIPPED
                if (currentStatus != OrderStatus.SHIPPED) {
                    return ServiceResult.error(HttpStatus.BAD_REQUEST,
                            "Solo se puede marcar como DELIVERED una orden física que esté SHIPPED.");
                }
            }
        }

        o.setStatus(newStatus);
        Order saved = orderRepo.save(o);
        return ServiceResult.ok(orderMapper.toResponse(saved));
    }

    // ====== Guest checkout: consultar orden ======
    @Override
    @Transactional(readOnly = true)
    public ServiceResult<OrderResponse> getGuestOrder(String email, String orderNumber) {
        if (email == null || email.isBlank()) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST, "Email requerido");
        }
        if (orderNumber == null || orderNumber.isBlank()) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST, "Número de orden requerido");
        }

        Order o = orderRepo.findByOrderNumberAndGuestEmail(orderNumber, email)
                .orElse(null);

        if (o == null) {
            return ServiceResult.error(HttpStatus.NOT_FOUND,
                    "No se encontró una orden con ese número y email.");
        }

        return ServiceResult.ok(orderMapper.toResponse(o));
    }

    @Override
    @Transactional
    public void linkGuestOrdersToUser(String email, Long userId) {
        if (email == null || email.isBlank() || userId == null) {
            return;
        }

        User user = userRepo.findById(userId).orElse(null);
        if (user == null) {
            return;
        }

        // Buscar solo las órdenes guest HUÉRFANAS con ese email (optimizado)
        List<Order> guestOrders = orderRepo.findByGuestEmailAndUserIsNull(email);

        if (guestOrders.isEmpty()) {
            return;
        }

        // Vincular cada orden al usuario
        for (Order order : guestOrders) {
            // Validación extra (aunque la query ya lo filtra)
            if (order.getUser() == null && email.equalsIgnoreCase(order.getGuestEmail())) {
                order.setUser(user);
                order.setUpdatedAt(LocalDateTime.now());
            }
        }

        orderRepo.saveAll(guestOrders);
    }
}
