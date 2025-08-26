// src/main/java/com/empresa/ecommerce_backend/web/CartCookieManager.java
package com.empresa.ecommerce_backend.web;

import com.empresa.ecommerce_backend.dto.response.CartResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CartCookieManager {

    public static final String CART_COOKIE = "cart_session";

    /** Setea la cookie si el carrito se creó o si cambió el sessionId. */
    public void maybeSetSessionCookie(String existingSessionId,
                                      ServiceResult<CartResponse> result,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        if (result == null || result.getData() == null) return;

        String newSessionId = result.getData().getSessionId();
        if (newSessionId == null || newSessionId.isBlank()) return;

        boolean created = result.getStatus() != null && result.getStatus().is2xxSuccessful()
                && result.getStatus().value() == 201; // CREATED

        boolean changed = existingSessionId == null || !existingSessionId.equals(newSessionId);

        if (created || changed) {
            setCookie(response, newSessionId, request.isSecure());
        }
    }

    private void setCookie(HttpServletResponse response, String sessionId, boolean secure) {
        ResponseCookie cookie = ResponseCookie.from(CART_COOKIE, sessionId)
                .httpOnly(true)
                .secure(secure)    // dev (HTTP): false, prod (HTTPS): true
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofDays(30))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
