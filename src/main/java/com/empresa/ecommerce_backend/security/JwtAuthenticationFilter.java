package com.empresa.ecommerce_backend.security;

import com.empresa.ecommerce_backend.service.interfaces.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.empresa.ecommerce_backend.service.JwtServiceImpl;

import java.io.IOException;

//1. JwtAuthenticationFilter
//🔍 Filtro que se ejecuta en cada request para:
//Revisar si viene un JWT en el header "Authorization"
//Validar si el token es correcto y no expiró
//Si es válido, autentica al usuario en el contexto de Spring Sec
// 📌 En otras palabras: ¿tiene token? ¿es válido? entonces confío en este usuario.


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

        // ⛔️ Ignorar filtros para rutas públicas
        if (path.startsWith("/api/login") ||
                path.startsWith("/api/register") ||
                path.startsWith("/api/verify-email") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/api-docs")) {

            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String token = jwtService.extractTokenFromHeader(authHeader);

        if (token != null && jwtService.isTokenValid(token)) {
            var authentication = jwtService.getAuthentication(token);
            ((AbstractAuthenticationToken) authentication)
                    .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
