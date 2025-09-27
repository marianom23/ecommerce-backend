// src/main/java/com/empresa/ecommerce_backend/controller/WishlistController.java
package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.request.AddProductToWishlistRequest;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.dto.response.WishlistResponse;
import com.empresa.ecommerce_backend.service.interfaces.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    /** Devuelve (o crea si no existe) la wishlist única del usuario. */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ServiceResult<WishlistResponse> getMyWishlist(
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        return wishlistService.getOrCreateForUser(userId);
    }

    /** Agrega un producto (sin cantidades). Idempotente: si ya está, no falla. */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/products")
    public ServiceResult<WishlistResponse> addProduct(
            @Valid @RequestBody AddProductToWishlistRequest dto,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        return wishlistService.addProduct(userId, dto.getProductId());
    }

    /** Quita un producto. Idempotente: si no estaba, no falla. */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/products/{productId}")
    public ServiceResult<WishlistResponse> removeProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        return wishlistService.removeProduct(userId, productId);
    }

    /** Alterna: agrega si no está, quita si ya está. */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/products/{productId}/toggle")
    public ServiceResult<WishlistResponse> toggleProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        return wishlistService.toggleProduct(userId, productId);
    }

    /** Vacía la wishlist (opcional, útil). */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping
    public ServiceResult<WishlistResponse> clear(
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        return wishlistService.clear(userId);
    }
}
