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
        Long uid = currentUserId();
        User user = userRepo.findById(uid)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Address shipAddr = addressRepo.findById(req.getShippingAddressId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Dirección de envío no encontrada"));

        BillingProfile bp = billingRepo.findById(req.getBillingProfileId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Perfil de facturación no encontrado"));

        Address billingAddr = bp.getBillingAddress();
        if (billingAddr == null) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST, "El perfil de facturación no tiene dirección asociada");
        }

        // Snapshots
        var shippingSnapshot = SnapshotMapper.toSnapshot(shipAddr);
        var billingSnapshot  = SnapshotMapper.toSnapshot(bp, billingAddr);

        // Crear Order
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress(shippingSnapshot);
        order.setBillingInfo(billingSnapshot);
        order.setChosenPaymentMethod(req.getPaymentMethod());

        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal shippingCost = calcularEnvio(shipAddr); // reemplazar con cotización real
        BigDecimal taxAmount = BigDecimal.ZERO;
        BigDecimal discountTotal = BigDecimal.ZERO;

        // Ítems
        for (var it : req.getItems()) {
            // Si tenés método de lock en el repo, úsalo; si no, findById
            ProductVariant v = variantRepo.findById(it.getVariantId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Variante no encontrada: " + it.getVariantId()));

            if (v.getStock() < it.getQuantity()) {
                return ServiceResult.error(HttpStatus.BAD_REQUEST, "Stock insuficiente para SKU " + v.getSku());
            }

            // Reservar stock (simplificado)
            v.setStock(v.getStock() - it.getQuantity());

            BigDecimal unitPrice = v.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(it.getQuantity()));

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setVariant(v);
            oi.setProductName(v.getProduct().getName());
            oi.setSku(v.getSku());
            oi.setAttributesJson(v.getAttributesJson());
            oi.setUnitPrice(unitPrice);
            oi.setQuantity(it.getQuantity());
            oi.setDiscountAmount(BigDecimal.ZERO);
            oi.setLineTotal(lineTotal);

            order.getItems().add(oi);
            subTotal = subTotal.add(lineTotal);
        }

        taxAmount = calcularImpuestos(subTotal);
        BigDecimal total = subTotal.add(shippingCost).add(taxAmount).subtract(discountTotal);

        order.setSubTotal(subTotal);
        order.setShippingCost(shippingCost);
        order.setTaxAmount(taxAmount);
        order.setDiscountTotal(discountTotal);
        order.setTotalAmount(total);

        Order saved = orderRepo.save(order);
        OrderResponse resp = orderMapper.toResponse(saved);
        return ServiceResult.created(resp);
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
