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

    @org.springframework.beans.factory.annotation.Value("${app.cookie.domain:}")
    private String cookieDomain;

    public void maybeSetSessionCookie(String existingSessionId,
                                      ServiceResult<CartResponse> result,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        if (result == null || result.getData() == null) return;

        String newSessionId = result.getData().getSessionId();
        if (newSessionId == null || newSessionId.isBlank()) return;

        boolean created = result.getStatus() != null && result.getStatus().is2xxSuccessful()
                && result.getStatus().value() == 201;

        boolean changed = existingSessionId == null || !existingSessionId.equals(newSessionId);

        if (created || changed) {
            setCookie(response, newSessionId, request.isSecure());
        }
    }

    public void setCookie(HttpServletResponse response, String sessionId, boolean secureRequest) {
        boolean prod = isProd();
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(CART_COOKIE, sessionId)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofDays(30));

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            b.domain(cookieDomain);
        }

        if (prod) {
            b.secure(true).sameSite("None");
        } else {
            b.secure(false).sameSite("Lax");
        }
        response.addHeader(HttpHeaders.SET_COOKIE, b.build().toString());
    }

    /** 👇 Borrar la cookie (al hacer logout) */
    public void clearSessionCookie(HttpServletResponse response, boolean secureRequest) {
        boolean prod = isProd();
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(CART_COOKIE, "")
                .httpOnly(true)
                .path("/")
                .maxAge(0); // elimina

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            b.domain(cookieDomain);
        }

        if (prod) {
            b.secure(true).sameSite("None");
        } else {
            b.secure(false).sameSite("Lax");
        }
        response.addHeader(HttpHeaders.SET_COOKIE, b.build().toString());
    }

    private boolean isProd() {
        String profile = System.getProperty("spring.profiles.active");
        if (profile == null) profile = System.getenv("SPRING_PROFILES_ACTIVE");
        return "prod".equalsIgnoreCase(profile);
    }
}
