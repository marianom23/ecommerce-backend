// src/main/java/com/empresa/ecommerce_backend/controller/ProductImageController.java
package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.service.interfaces.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List; // 👈 List
// ↑ también necesitás DeleteMapping, PutMapping, RequestBody

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService imageAppService; // un service “de aplicación” que devuelve ServiceResult

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{productId}/images")
    public ServiceResult<Void> addProductImages(
            @PathVariable Long productId,
            @RequestBody List<String> urls) {
        return imageAppService.addProductImages(productId, urls); // devuelve ServiceResult
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{productId}/variants/{variantId}/images")
    public ServiceResult<Void> addVariantImages(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @RequestBody List<String> urls) {
        return imageAppService.addVariantImages(productId, variantId, urls);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{productId}/images/{imageId}")
    public ServiceResult<Void> deleteImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        return imageAppService.deleteImage(productId, imageId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{productId}/images/reorder")
    public ServiceResult<Void> reorderImages(
            @PathVariable Long productId,
            @RequestBody List<Long> imageIdsInOrder) {
        return imageAppService.reorderImages(productId, imageIdsInOrder);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{productId}/variants/{variantId}/images/reorder")
    public ServiceResult<Void> reorderVariantImages(
            @PathVariable Long productId,
            @PathVariable Long variantId,
            @RequestBody List<Long> imageIdsInOrder) {
        return imageAppService.reorderVariantImages(productId, variantId, imageIdsInOrder);
    }
}
