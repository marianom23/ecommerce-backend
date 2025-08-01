// src/main/java/com/empresa/ecommerce_backend/controller/PurchaseOrderController.java
package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.request.PurchaseOrderRequest;
import com.empresa.ecommerce_backend.dto.response.PurchaseOrderResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.service.interfaces.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ServiceResult<PurchaseOrderResponse> createPurchaseOrder(@RequestBody @Valid PurchaseOrderRequest request) {
        return purchaseOrderService.createPurchaseOrder(request); // -> 201 Created vía ServiceResult.created(...)
    }
}
