// src/main/java/com/empresa/ecommerce_backend/controller/OrderController.java
package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.request.ConfirmOrderRequest;
import com.empresa.ecommerce_backend.dto.request.UpdateBillingProfileRequest;
import com.empresa.ecommerce_backend.dto.request.UpdatePaymentMethodRequest;
import com.empresa.ecommerce_backend.dto.request.UpdateShippingAddressRequest;
import com.empresa.ecommerce_backend.dto.response.OrderResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.service.interfaces.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Crear orden desde el carrito del usuario autenticado (SIN BODY)
    @PostMapping
    public ServiceResult<OrderResponse> create() {
        return orderService.createOrder(); // << nuevo
    }

    // Obtener una orden del usuario autenticado
    @GetMapping("/{id}")
    public ServiceResult<OrderResponse> getOne(@PathVariable Long id) {
        return orderService.getOne(id);
    }

    // Listar todas mis órdenes
    @GetMapping
    public ServiceResult<List<OrderResponse>> listMine() {
        return orderService.listMine();
    }

    // Agregar/actualizar shipping
    @PatchMapping("/{id}/shipping-address")
    public ServiceResult<OrderResponse> patchShipping(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateShippingAddressRequest req) {
        return orderService.patchShippingAddress(id, req);
    }

    // Agregar/actualizar facturación
    @PatchMapping("/{id}/billing-profile")
    public ServiceResult<OrderResponse> patchBilling(@PathVariable Long id,
                                                     @Valid @RequestBody UpdateBillingProfileRequest req) {
        return orderService.patchBillingProfile(id, req);
    }

    // Cambiar método de pago mientras esté PENDING
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


}
