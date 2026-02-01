// src/main/java/com/empresa/ecommerce_backend/controller/OrderController.java
package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.request.*;
import com.empresa.ecommerce_backend.dto.response.*;
import com.empresa.ecommerce_backend.service.interfaces.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ServiceResult<OrderResponse> create(@RequestBody(required = false) CreateOrderRequest req) {
        return orderService.createOrder(req);
    }

    @GetMapping("/{id}")
    public ServiceResult<OrderResponse> getOne(@PathVariable Long id) {
        return orderService.getOne(id);
    }

    /** EXISTENTE: lista completa (detalle) - mantener por compatibilidad */
    @GetMapping
    public ServiceResult<List<OrderResponse>> listMine() {
        return orderService.listMine();
    }

    @GetMapping("/by-number/{orderNumber}")
    public ServiceResult<OrderResponse> getOneByNumber(@PathVariable String orderNumber) {
        return orderService.getOneByNumber(orderNumber);
    }

    /** NUEVO: lista resumida paginada */
    @GetMapping("/summary")
    public ServiceResult<PageResponse<OrderSummaryResponse>> listSummaries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate,desc") String sort) {
        // parseo simple "campo,direccion"
        Sort sortObj;
        if (sort.contains(",")) {
            String[] parts = sort.split(",", 2);
            sortObj = "asc".equalsIgnoreCase(parts[1])
                    ? Sort.by(parts[0]).ascending()
                    : Sort.by(parts[0]).descending();
        } else {
            sortObj = Sort.by(sort).descending();
        }
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return orderService.listMineSummaries(pageable);
    }

    @PatchMapping("/{orderNumber}/shipping-address")
    public ServiceResult<OrderResponse> patchShipping(@PathVariable String orderNumber,
            @Valid @RequestBody UpdateShippingAddressRequest req) {
        return orderService.patchShippingAddress(orderNumber, req);
    }

    @PatchMapping("/{orderNumber}/billing-profile")
    public ServiceResult<OrderResponse> patchBilling(@PathVariable String orderNumber,
            @Valid @RequestBody UpdateBillingProfileRequest req) {
        return orderService.patchBillingProfile(orderNumber, req);
    }

    @PatchMapping("/{orderNumber}/payment-method")
    public ServiceResult<OrderResponse> patchPaymentMethod(@PathVariable String orderNumber,
            @Valid @RequestBody UpdatePaymentMethodRequest req) {
        return orderService.patchPaymentMethod(orderNumber, req);
    }

    @PostMapping("/{orderNumber}/confirm")
    public ServiceResult<OrderResponse> confirm(@PathVariable String orderNumber,
            @RequestBody(required = false) ConfirmOrderRequest req) {
        return orderService.confirmOrder(orderNumber, req);
    }

    @PatchMapping("/{id}/status")
    public ServiceResult<OrderResponse> updateStatus(@PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest req) {
        return orderService.updateOrderStatus(id, req);
    }

    /** Endpoint público para que guests consulten sus órdenes */
    @GetMapping("/guest")
    public ServiceResult<OrderResponse> getGuestOrder(
            @RequestParam String email,
            @RequestParam String orderNumber) {
        return orderService.getGuestOrder(email, orderNumber);
    }
}
