// src/main/java/com/empresa/ecommerce_backend/service/OrderServiceImpl.java
package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.CreateOrderRequest;
import com.empresa.ecommerce_backend.dto.request.UpdatePaymentMethodRequest;
import com.empresa.ecommerce_backend.dto.response.OrderResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.enums.OrderStatus;
import com.empresa.ecommerce_backend.exception.RecursoNoEncontradoException;
import com.empresa.ecommerce_backend.mapper.OrderMapper;
import com.empresa.ecommerce_backend.mapper.SnapshotMapper;
import com.empresa.ecommerce_backend.model.*;
import com.empresa.ecommerce_backend.repository.*;
import com.empresa.ecommerce_backend.service.interfaces.OrderService;
import lombok.RequiredArgsConstructor;
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

    // 👇 inyectamos carrito
    private final CartRepository cartRepo;

    private final OrderMapper orderMapper;

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
    @Transactional
    public ServiceResult<OrderResponse> createOrder(CreateOrderRequest req) {
        Long uid = null;
        try { uid = currentUserId(); } catch (Exception ignored) {}

        // 1) Usuario (si hay)
        User user = (uid != null) ? userRepo.findById(uid).orElse(null) : null;

        // 2) Direcciones / facturación
        Address shipAddr = addressRepo.findById(req.getShippingAddressId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Dirección de envío no encontrada"));

        BillingProfile bp = billingRepo.findById(req.getBillingProfileId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Perfil de facturación no encontrado"));

        Address billingAddr = bp.getBillingAddress();
        if (billingAddr == null) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST, "El perfil de facturación no tiene dirección asociada");
        }

        // 3) Resolver carrito (usuario o sesión)
        Cart cart = null;
        if (user != null) cart = cartRepo.findByUserId(user.getId()).orElse(null);
        if (cart == null && req.getSessionId() != null && !req.getSessionId().isBlank()) {
            cart = cartRepo.findBySessionId(req.getSessionId()).orElse(null);
        }
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST, "Tu carrito está vacío.");
        }

        // 4) Crear orden + snapshots
        var shippingSnapshot = SnapshotMapper.toSnapshot(shipAddr);
        var billingSnapshot  = SnapshotMapper.toSnapshot(bp, billingAddr);

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress(shippingSnapshot);
        order.setBillingInfo(billingSnapshot);
        order.setChosenPaymentMethod(req.getPaymentMethod());
        // ⚠️ Dejamos llegar couponCode pero no lo usamos por ahora.
        // order.setCouponCode(null); // si tenés la columna y querés dejarla vacía

        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal shippingCost = calcularEnvio(shipAddr); // TODO: integrar cotización real
        BigDecimal taxAmount = BigDecimal.ZERO;

        // 5) Ítems desde carrito (revalidar stock/precio)
        for (CartItem ci : cart.getItems()) {
            ProductVariant v = ci.getVariant();
            if (v == null) {
                return ServiceResult.error(HttpStatus.BAD_REQUEST, "Ítem inválido en el carrito (falta variante).");
            }
            int stock = v.getStock() == null ? 0 : v.getStock();
            int qty = ci.getQuantity();
            if (qty <= 0) continue;
            if (qty > stock) {
                return ServiceResult.error(HttpStatus.CONFLICT, "Stock insuficiente para SKU " + v.getSku());
            }

            BigDecimal unitPrice = v.getPrice(); // repricing actual
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setVariant(v);
            oi.setProductName(v.getProduct().getName());
            oi.setSku(v.getSku());
            oi.setAttributesJson(v.getAttributesJson());
            oi.setUnitPrice(unitPrice);
            oi.setQuantity(qty);
            oi.setDiscountAmount(BigDecimal.ZERO); // sin descuentos por ítem por ahora
            oi.setLineTotal(lineTotal);

            // Reservar stock (simple)
            v.setStock(stock - qty);

            order.getItems().add(oi);
            subTotal = subTotal.add(lineTotal);
        }

        // 6) Descuentos/Impuestos/Total (sin cupón)
        BigDecimal discountTotal = BigDecimal.ZERO; // 👈 ignoramos couponCode por ahora
        taxAmount = calcularImpuestos(subTotal);    // si corresponde, sobre bruto

        BigDecimal total = subTotal.subtract(discountTotal).add(shippingCost).add(taxAmount);
        if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;

        order.setSubTotal(subTotal);
        order.setShippingCost(shippingCost);
        order.setTaxAmount(taxAmount);
        order.setDiscountTotal(discountTotal);
        order.setTotalAmount(total);

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

    // ===== NUEVO: GET /api/orders (listar mías) =====
    @Override
    @Transactional(readOnly = true)
    public ServiceResult<List<OrderResponse>> listMine() {
        Long uid = currentUserId();
        var list = orderRepo.findAllByUserIdOrderByCreatedAtDesc(uid).stream()
                .map(orderMapper::toResponse)
                .toList();
        return ServiceResult.ok(list);
    }

    // ===== NUEVO: PATCH /api/orders/{id}/payment-method =====
    @Override
    @Transactional
    public ServiceResult<OrderResponse> patchPaymentMethod(Long orderId, UpdatePaymentMethodRequest req) {
        Long uid = currentUserId();
        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));

        if (!isOwner(o, uid)) {
            return ServiceResult.error(HttpStatus.FORBIDDEN, "No puede modificar esta orden.");
        }
        if (o.getStatus() != OrderStatus.PENDING) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST, "Solo puede cambiar el método de pago mientras la orden está PENDING.");
        }
        // si ya existe un Payment en proceso/confirmado, podrías bloquear aquí:
        // if (o.getPayment() != null && o.getPayment().getStatus() != PaymentStatus.CANCELED) { ... }

        o.setChosenPaymentMethod(req.getPaymentMethod());

        Order saved = orderRepo.save(o);
        return ServiceResult.ok(orderMapper.toResponse(saved));
    }

    private boolean isOwner(Order order, Long userId) {
        return order.getUser() != null
                && order.getUser().getId() != null
                && order.getUser().getId().equals(userId);
    }


    private BigDecimal calcularEnvio(Address shipAddr) {
        // TODO: integrar cotización real (Andreani/OCA/etc.)
        return new BigDecimal("0.00");
    }

    private BigDecimal calcularImpuestos(BigDecimal subTotal) {
        // TODO: IVA/impuestos reales
        return subTotal.multiply(new BigDecimal("0.00"));
    }
}
