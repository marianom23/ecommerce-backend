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
    public ServiceResult<OrderResponse> create() {
        return orderService.createOrder();
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
            @RequestParam(defaultValue = "orderDate,desc") String sort
    ) {
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

    @PatchMapping("/{id}/shipping-address")
    public ServiceResult<OrderResponse> patchShipping(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateShippingAddressRequest req) {
        return orderService.patchShippingAddress(id, req);
    }

    @PatchMapping("/{id}/billing-profile")
    public ServiceResult<OrderResponse> patchBilling(@PathVariable Long id,
                                                     @Valid @RequestBody UpdateBillingProfileRequest req) {
        return orderService.patchBillingProfile(id, req);
    }

    @PatchMapping("/{id}/payment-method")
    public ServiceResult<OrderResponse> patchPaymentMethod(@PathVariable Long id,
                                                           @Valid @RequestBody UpdatePaymentMethodRequest req) {
        return orderService.patchPaymentMethod(id, req);
    }

    @PostMapping("/{id}/confirm")
    public ServiceResult<OrderResponse> confirm(@PathVariable Long id,
                                                @RequestBody(required = false) ConfirmOrderRequest req) {
        return orderService.confirmOrder(id, req);
    }

    @PatchMapping("/{id}/status")
    public ServiceResult<OrderResponse> updateStatus(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateOrderStatusRequest req) {
        return orderService.updateOrderStatus(id, req);
    }
}
