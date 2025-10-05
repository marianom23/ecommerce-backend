// src/main/java/com/empresa/ecommerce_backend/controller/AuthController.java
package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.request.LoginRequest;
import com.empresa.ecommerce_backend.dto.request.OAuthCallbackRequest;
import com.empresa.ecommerce_backend.dto.request.RegisterUserRequest;
import com.empresa.ecommerce_backend.dto.response.LoginResponse;
import com.empresa.ecommerce_backend.dto.response.RegisterUserResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.service.UserServiceImpl;
import com.empresa.ecommerce_backend.web.CartCookieManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UserServiceImpl userServiceImpl;
    private final CartCookieManager cookieManager; // solo para /logout

    /* -------- Registro -------- */
    @PostMapping("/register")
    public ServiceResult<RegisterUserResponse> register(@Valid @RequestBody RegisterUserRequest dto) {
        return userServiceImpl.registerUser(dto);
    }

    /* -------- Verificación de correo -------- */
    @GetMapping("/verify-email")
    public ServiceResult<Void> verifyEmail(@RequestParam("token") String token) {
        return userServiceImpl.verifyEmail(token);
    }

    /* -------- Login -------- */
    @PostMapping("/login")
    public ServiceResult<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest
    ) {
        String ip = extractClientIp(servletRequest);
        return userServiceImpl.login(request, ip);
    }

    /* -------- Callback OAuth2 -------- */
    @PostMapping("/oauth2/callback")
    public ServiceResult<LoginResponse> handleOAuthCallback(
            @RequestBody OAuthCallbackRequest dto,
            HttpServletRequest servletRequest
    ) {
        String ip = extractClientIp(servletRequest);
        // sin lógica de carrito aquí
        return userServiceImpl.handleOAuthCallback(dto, ip);
    }

    /* -------- Perfil -------- */
    @GetMapping("/me")
    public Object getProfile(
            @org.springframework.security.core.annotation.AuthenticationPrincipal
            org.springframework.security.core.userdetails.UserDetails userDetails
    ) {
        return userDetails;
    }

    /* -------- Logout -------- */
    @PostMapping("/logout")
    public ServiceResult<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        // borrar cookie cart_session
        cookieManager.clearSessionCookie(response, request.isSecure());
        return ServiceResult.noContent(); // 204
    }

    /* -------- Utilidad para IP -------- */
    private String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        return (xfHeader != null && !xfHeader.isBlank())
                ? xfHeader.split(",")[0]
                : request.getRemoteAddr();
    }
}
