package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.request.ProductVariantRequest;
import com.empresa.ecommerce_backend.dto.response.ProductVariantResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.service.interfaces.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/products/{productId}/variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService variantService;

    @GetMapping
    public ServiceResult<List<ProductVariantResponse>> list(@PathVariable Long productId) {
        return variantService.listByProduct(productId);
    }

    @GetMapping("/{variantId}")
    public ServiceResult<ProductVariantResponse> getOne(
            @PathVariable Long productId, @PathVariable Long variantId) {
        return variantService.getOne(productId, variantId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ServiceResult<ProductVariantResponse> create(
            @PathVariable Long productId, @Valid @RequestBody ProductVariantRequest req) {
        return variantService.create(productId, req);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{variantId}") // o PATCH si preferís parcial
    public ServiceResult<ProductVariantResponse> update(
            @PathVariable Long productId, @PathVariable Long variantId,
            @Valid @RequestBody ProductVariantRequest req) {
        return variantService.update(productId, variantId, req);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{variantId}")
    public ServiceResult<Void> delete(
            @PathVariable Long productId, @PathVariable Long variantId) {
        return variantService.delete(productId, variantId);
    }
}
