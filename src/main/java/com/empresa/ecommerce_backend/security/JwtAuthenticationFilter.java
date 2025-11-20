// src/main/java/com/empresa/ecommerce_backend/security/JwtAuthenticationFilter.java
package com.empresa.ecommerce_backend.security;

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

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Rutas públicas que no requieren JWT
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

        // 1) Intentar leer de Authorization: Bearer xxx
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            System.out.println("🔑 JWT encontrado en Authorization header");
        }

        // 2) Si no, leer cookie httpOnly "auth_token"
        if (token == null) {
            Cookie[] cookies = request.getCookies();
            System.out.println("🔍 Buscando JWT en cookies...");
            if (cookies != null) {
                for (Cookie c : cookies) {
                    System.out.println("   Cookie -> " + c.getName());
                    if (AuthCookieManager.AUTH_COOKIE.equals(c.getName())) {
                        token = c.getValue();
                        System.out.println("✅ auth_token encontrado en cookies");
                        break;
                    }
                }
            } else {
                System.out.println("   No hay cookies en la request");
            }
        }

        if (token != null) {
            try {
                // Delega en JwtService para validar y construir Authentication
                Authentication authentication = jwtService.getAuthentication(token);

                if (authentication != null) {
                    ((AbstractAuthenticationToken) authentication)
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("✅ Authentication seteada para principal: "
                            + authentication.getPrincipal());
                } else {
                    System.out.println("⚠️ jwtService.getAuthentication(token) devolvió null");
                }
            } catch (Exception e) {
                System.out.println("❌ Error validando JWT: " + e.getMessage());
                SecurityContextHolder.clearContext();
            }
        } else {
            System.out.println("ℹ️ No se encontró token JWT ni en header ni en cookie");
        }

        filterChain.doFilter(request, response);
    }
}
