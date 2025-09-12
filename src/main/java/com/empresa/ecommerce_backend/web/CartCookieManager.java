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

    private void setCookie(HttpServletResponse response, String sessionId, boolean secureRequest) {
        boolean prod = isProd(); // podés basarte en SPRING_PROFILES_ACTIVE o en secureRequest

        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(CART_COOKIE, sessionId)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofDays(30));

        if (prod) {
            builder.secure(true);
            builder.sameSite("None"); // para cross-site en prod
        } else {
            builder.secure(false);
            builder.sameSite("Lax"); // para dev en http://localhost
        }

        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }

    private boolean isProd() {
        String profile = System.getProperty("spring.profiles.active");
        if (profile == null) {
            profile = System.getenv("SPRING_PROFILES_ACTIVE");
        }
        return "prod".equalsIgnoreCase(profile);
    }

}
