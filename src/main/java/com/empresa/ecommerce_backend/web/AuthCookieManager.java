// src/main/java/com/empresa/ecommerce_backend/web/AuthCookieManager.java
package com.empresa.ecommerce_backend.web;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthCookieManager {

    public static final String AUTH_COOKIE = "auth_token";

    public void setAuthCookie(HttpServletResponse response, String jwtToken, boolean secureRequest) {
        boolean prod = isProd();

        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(AUTH_COOKIE, jwtToken)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofDays(1)); // ajustá a la vida útil de tu JWT

        if (prod) {
            b.secure(true).sameSite("None");
        } else {
            b.secure(false).sameSite("Lax");
        }

        response.addHeader(HttpHeaders.SET_COOKIE, b.build().toString());
    }

    public void clearAuthCookie(HttpServletResponse response, boolean secureRequest) {
        boolean prod = isProd();

        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(AUTH_COOKIE, "")
                .httpOnly(true)
                .path("/")
                .maxAge(0); // elimina la cookie

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
