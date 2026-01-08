package com.empresa.ecommerce_backend.web;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshTokenCookieManager {

    public static final String REFRESH_COOKIE = "refresh_token";

    public void setRefreshCookie(HttpServletResponse response, String token, boolean secureRequest) {
        boolean prod = isProd();
        
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(REFRESH_COOKIE, token)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofDays(7));

        if (prod) {
            builder.secure(true).sameSite("None");
        } else {
            builder.secure(false).sameSite("Lax");
        }

        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }

    public void clearRefreshCookie(HttpServletResponse response, boolean secureRequest) {
        boolean prod = isProd();
        
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .path("/")
                .maxAge(0);

        if (prod) {
            builder.secure(true).sameSite("None");
        } else {
            builder.secure(false).sameSite("Lax");
        }

        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }

    private boolean isProd() {
        String profile = System.getProperty("spring.profiles.active");
        if (profile == null) profile = System.getenv("SPRING_PROFILES_ACTIVE");
        return "prod".equalsIgnoreCase(profile);
    }
}
