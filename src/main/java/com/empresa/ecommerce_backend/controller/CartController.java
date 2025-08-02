// src/main/java/com/empresa/ecommerce_backend/controller/CartController.java
package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.request.AddItemRequest;
import com.empresa.ecommerce_backend.dto.request.UpdateQtyRequest;
import com.empresa.ecommerce_backend.dto.response.CartResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.service.interfaces.CartService;
import com.empresa.ecommerce_backend.web.CartCookieManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CartCookieManager cookieManager;

    @GetMapping("/me")
    public ServiceResult<CartResponse> getMyCart(
            @CookieValue(value = CartCookieManager.CART_COOKIE, required = false) String sessionId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        var result = cartService.getOrCreateBySession(sessionId);
        cookieManager.maybeSetSessionCookie(sessionId, result, request, response);
        return result;
    }

    @PostMapping("/items")
    public ServiceResult<CartResponse> addItem(
            @Valid @RequestBody AddItemRequest dto,
            @CookieValue(value = CartCookieManager.CART_COOKIE, required = false) String sessionId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        var result = cartService.addItem(sessionId, dto);
        cookieManager.maybeSetSessionCookie(sessionId, result, request, response);
        return result;
    }

    @PatchMapping("/items/{itemId}")
    public ServiceResult<CartResponse> updateQuantity(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateQtyRequest dto,
            @CookieValue(value = CartCookieManager.CART_COOKIE, required = false) String sessionId
    ) {
        return cartService.updateQuantity(sessionId, itemId, dto);
    }

    @DeleteMapping("/items/{itemId}")
    public ServiceResult<CartResponse> removeItem(
            @PathVariable Long itemId,
            @CookieValue(value = CartCookieManager.CART_COOKIE, required = false) String sessionId
    ) {
        return cartService.removeItem(sessionId, itemId);
    }

    @DeleteMapping
    public ServiceResult<CartResponse> clear(
            @CookieValue(value = CartCookieManager.CART_COOKIE, required = false) String sessionId
    ) {
        return cartService.clear(sessionId);
    }
}
