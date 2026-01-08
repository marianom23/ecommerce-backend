package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.response.LoginResponse;
import com.empresa.ecommerce_backend.service.interfaces.CartService;
import com.empresa.ecommerce_backend.service.interfaces.JwtService;
import com.empresa.ecommerce_backend.web.CartCookieManager;
import com.empresa.ecommerce_backend.web.RefreshTokenCookieManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Procesa acciones post-login comunes a login normal y OAuth:
 * - Fusionar carrito guest con usuario
 * - Generar access + refresh tokens
 * - Setear cookies
 */
@Service
@RequiredArgsConstructor
public class PostLoginProcessor {

    private final CartService cartService;
    private final JwtService jwtService;
    private final CartCookieManager cartCookieManager;
    private final RefreshTokenCookieManager refreshTokenCookieManager;

    /**
     * Procesa un login exitoso: fusiona carrito, genera tokens, actualiza cookies.
     */
    public void process(LoginResponse loginData, HttpServletRequest request, HttpServletResponse response) {
        // 1. Fusionar carrito guest con usuario
        String guestSessionId = extractCartSessionId(request);
        if (guestSessionId != null && !guestSessionId.isBlank()) {
            var cartResult = cartService.attachCartToUser(guestSessionId, loginData.getId());
            cartCookieManager.maybeSetSessionCookie(guestSessionId, cartResult, request, response);
        }

        // 2. Generar tokens
        String accessToken = jwtService.generateAccessToken(
            loginData.getId(), 
            loginData.getEmail(), 
            loginData.getRoles()
        );
        String refreshToken = jwtService.generateRefreshToken(loginData.getId());

        // 3. Setear refresh token en cookie HttpOnly
        refreshTokenCookieManager.setRefreshCookie(response, refreshToken, request.isSecure());

        // 4. Actualizar response con access token
        loginData.setToken(accessToken);
    }

    /**
     * Extrae cart_session de la cookie.
     */
    private String extractCartSessionId(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("cart_session".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
