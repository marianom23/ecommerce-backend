package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.request.AddItemRequest;
import com.empresa.ecommerce_backend.dto.request.UpdateQtyRequest;
import com.empresa.ecommerce_backend.dto.response.CartResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.security.AuthUser;
import com.empresa.ecommerce_backend.service.MetaPixelService;
import com.empresa.ecommerce_backend.service.interfaces.CartService;
import com.empresa.ecommerce_backend.web.CartCookieManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CartCookieManager cookieManager;
    private final MetaPixelService metaPixelService;

    /* =================== ATTACH =================== */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/attach")
    public ServiceResult<CartResponse> attachToUser(
            @CookieValue(value = CartCookieManager.CART_COOKIE, required = false) String cookieSessionId,
            @RequestHeader(value = "X-Cart-Session", required = false) String headerSessionId,
            @AuthenticationPrincipal AuthUser me,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String sessionId = resolveSessionId(headerSessionId, cookieSessionId);
        Long userId = me.getId();
        var result = cartService.attachCartToUser(sessionId, userId);
        cookieManager.maybeSetSessionCookie(sessionId, result, request, response);
        return result;
    }

    /* =================== GET MY CART =================== */
    @GetMapping("/me")
    public ServiceResult<CartResponse> getMyCart(
            @CookieValue(value = CartCookieManager.CART_COOKIE, required = false) String cookieSessionId,
            @RequestHeader(value = "X-Cart-Session", required = false) String headerSessionId,
            @AuthenticationPrincipal AuthUser me,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String sessionId = resolveSessionId(headerSessionId, cookieSessionId);
        // ✅ Si hay usuario autenticado, obtener SU carrito (la fusión ya pasó en login)
        if (me != null) {
            Long userId = me.getId();
            var result = cartService.getOrCreate(userId, null);
            return result;
        }

        // 👤 Guest → obtener/crear carrito guest
        var result = cartService.getOrCreate(null, sessionId);
        cookieManager.maybeSetSessionCookie(sessionId, result, request, response);
        return result;
    }

    /* =================== ADD ITEM =================== */
    @PostMapping("/items")
    public ServiceResult<CartResponse> addItem(
            @Valid @RequestBody AddItemRequest dto,
            @CookieValue(value = CartCookieManager.CART_COOKIE, required = false) String cookieSessionId,
            @RequestHeader(value = "X-Cart-Session", required = false) String headerSessionId,
            @AuthenticationPrincipal AuthUser me,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String sessionId = resolveSessionId(headerSessionId, cookieSessionId);
        Long userId = (me != null) ? me.getId() : null;
        var result = cartService.addItem(userId, sessionId, dto);
        cookieManager.maybeSetSessionCookie(sessionId, result, request, response);
        
        // 📊 Enviar evento AddToCart a Meta
        if (result.getData() != null) {
            // No enviamos info del usuario para AddToCart (privacy-first)
            metaPixelService.sendEvent(
                "AddToCart",
                request,
                null,
                null // No value for AddToCart
            );
        }
        
        return result;
    }

    /* =================== UPDATE QTY =================== */
    @PatchMapping("/items/{itemId}")
    public ServiceResult<CartResponse> updateQuantity(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateQtyRequest dto,
            @CookieValue(value = CartCookieManager.CART_COOKIE, required = false) String cookieSessionId,
            @RequestHeader(value = "X-Cart-Session", required = false) String headerSessionId,
            @AuthenticationPrincipal AuthUser me
    ) {
        String sessionId = resolveSessionId(headerSessionId, cookieSessionId);
        Long userId = (me != null) ? me.getId() : null;
        return cartService.updateQuantity(userId, sessionId, itemId, dto);
    }

    /* =================== INCREMENT ITEM =================== */
    @PatchMapping("/items/{itemId}/increment")
    public ServiceResult<CartResponse> incrementItem(
            @PathVariable Long itemId,
            @CookieValue(value = CartCookieManager.CART_COOKIE, required = false) String cookieSessionId,
            @RequestHeader(value = "X-Cart-Session", required = false) String headerSessionId,
            @AuthenticationPrincipal AuthUser me
    ) {
        String sessionId = resolveSessionId(headerSessionId, cookieSessionId);
        Long userId = (me != null) ? me.getId() : null;
        return cartService.incrementItem(userId, sessionId, itemId);
    }

    /* =================== DECREMENT ITEM =================== */
    @PatchMapping("/items/{itemId}/decrement")
    public ServiceResult<CartResponse> decrementItem(
            @PathVariable Long itemId,
            @CookieValue(value = CartCookieManager.CART_COOKIE, required = false) String cookieSessionId,
            @RequestHeader(value = "X-Cart-Session", required = false) String headerSessionId,
            @AuthenticationPrincipal AuthUser me
    ) {
        String sessionId = resolveSessionId(headerSessionId, cookieSessionId);
        Long userId = (me != null) ? me.getId() : null;
        return cartService.decrementItem(userId, sessionId, itemId);
    }

    /* =================== REMOVE ITEM =================== */
    @DeleteMapping("/items/{itemId}")
    public ServiceResult<CartResponse> removeItem(
            @PathVariable Long itemId,
            @CookieValue(value = CartCookieManager.CART_COOKIE, required = false) String cookieSessionId,
            @RequestHeader(value = "X-Cart-Session", required = false) String headerSessionId,
            @AuthenticationPrincipal AuthUser me
    ) {
        String sessionId = resolveSessionId(headerSessionId, cookieSessionId);
        Long userId = (me != null) ? me.getId() : null;
        return cartService.removeItem(userId, sessionId, itemId);
    }

    /* =================== CLEAR CART =================== */
    @DeleteMapping
    public ServiceResult<CartResponse> clear(
            @CookieValue(value = CartCookieManager.CART_COOKIE, required = false) String cookieSessionId,
            @RequestHeader(value = "X-Cart-Session", required = false) String headerSessionId,
            @AuthenticationPrincipal AuthUser me
    ) {
        String sessionId = resolveSessionId(headerSessionId, cookieSessionId);
        Long userId = (me != null) ? me.getId() : null;
        return cartService.clear(userId, sessionId);
    }

    /**
     * Resuelve el sessionId con prioridad: header > cookie
     * Esto permite funcionar en modo incógnito (donde las cookies fallan)
     */
    private String resolveSessionId(String headerSessionId, String cookieSessionId) {
        return headerSessionId != null ? headerSessionId : cookieSessionId;
    }
}
