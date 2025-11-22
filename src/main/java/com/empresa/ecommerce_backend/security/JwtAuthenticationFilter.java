// src/main/java/com/empresa/ecommerce_backend/security/JwtAuthenticationFilter.java
package com.empresa.ecommerce_backend.security;

import com.empresa.ecommerce_backend.repository.UserRepository;
import com.empresa.ecommerce_backend.service.interfaces.JwtService;
import com.empresa.ecommerce_backend.web.AuthCookieManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Rutas públicas
        if (path.startsWith("/api/login")
                || path.startsWith("/api/register")
                || path.startsWith("/api/verify-email")
                || path.startsWith("/api/oauth2/callback")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Preflight CORS
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = null;

        // 1) Authorization: Bearer xxx
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // 2) Cookie auth_token
        if (token == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if (AuthCookieManager.AUTH_COOKIE.equals(c.getName())) {
                        token = c.getValue();
                        break;
                    }
                }
            }
        }

        if (token != null) {
            try {
                Authentication authentication = jwtService.getAuthentication(token);

                if (authentication != null) {
                    Object principal = authentication.getPrincipal();
                    Long userId = null;

                    if (principal instanceof AuthUser authUser) {
                        userId = authUser.getId();
                    } else if (principal instanceof String s) {
                        try {
                            userId = Long.parseLong(s);
                        } catch (NumberFormatException ignored) {
                        }
                    }

                    if (userId != null && userRepository.existsById(userId)) {
                        ((AbstractAuthenticationToken) authentication)
                                .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        SecurityContextHolder.clearContext();
                        clearAuthCookie(response);
                    }

                } else {
                    SecurityContextHolder.clearContext();
                    clearAuthCookie(response);
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                clearAuthCookie(response);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void clearAuthCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(AuthCookieManager.AUTH_COOKIE, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0); // borrar
        response.addCookie(cookie);
    }
}
