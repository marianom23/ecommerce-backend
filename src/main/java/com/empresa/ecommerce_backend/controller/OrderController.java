// src/main/java/com/empresa/ecommerce_backend/controller/OrderController.java
package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.request.CreateOrderRequest;
import com.empresa.ecommerce_backend.dto.request.UpdatePaymentMethodRequest;
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

    // Crear orden
    @PostMapping
    public ServiceResult<OrderResponse> create(@Valid @RequestBody CreateOrderRequest req) {
        return orderService.createOrder(req);
    }

    // Obtener una orden del usuario autenticado
    @GetMapping("/{id}")
    public ServiceResult<OrderResponse> getOne(@PathVariable Long id) {
        return orderService.getOne(id);
    }

    // Listar todas mis órdenes (usuario autenticado)
    @GetMapping
    public ServiceResult<List<OrderResponse>> listMine() {
        return orderService.listMine();
    }

    // Cambiar método de pago mientras esté PENDING
    @PatchMapping("/{id}/payment-method")
    public ServiceResult<OrderResponse> patchPaymentMethod(@PathVariable Long id,
                                                           @Valid @RequestBody UpdatePaymentMethodRequest req) {
        return orderService.patchPaymentMethod(id, req);
    }
}
