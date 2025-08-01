// src/main/java/com/empresa/ecommerce_backend/controller/AuthController.java
package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.request.LoginRequest;
import com.empresa.ecommerce_backend.dto.request.OAuthCallbackRequest;
import com.empresa.ecommerce_backend.dto.request.RegisterUserRequest;
import com.empresa.ecommerce_backend.dto.response.RegisterUserResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.service.UserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UserServiceImpl userServiceImpl;

    /* -------- Registro -------- */
    @PostMapping("/register")
    public ServiceResult<RegisterUserResponse> register(
            @Valid @RequestBody RegisterUserRequest dto) {
        return userServiceImpl.registerUser(dto);           // devuelve created(...)
    }

    /* -------- Verificación de correo -------- */
    @GetMapping("/verify-email")
    public ServiceResult<Void> verifyEmail(@RequestParam("token") String token) {
        return userServiceImpl.verifyEmail(token);          // ok() o error(...)
    }

    /* -------- Login -------- */
    @PostMapping("/login")
    public ServiceResult<?> login(@Valid @RequestBody LoginRequest request,
                                  HttpServletRequest servletRequest) {
        String ip = extractClientIp(servletRequest);
        return userServiceImpl.login(request, ip);          // ok()
    }

    /* -------- Callback OAuth2 (Google / Azure AD) -------- */
    @PostMapping("/oauth2/callback")
    public ServiceResult<String> handleOAuthCallback(@RequestBody OAuthCallbackRequest dto,
                                                     HttpServletRequest servletRequest) {
        String ip = extractClientIp(servletRequest);
        return userServiceImpl.handleOAuthCallback(dto, ip); // ok()
    }

    /* -------- Perfil del usuario autenticado -------- */
    @GetMapping("/me")
    public UserDetails getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return userDetails;                                 // 200 OK plano
    }

    /* -------- Logout (stateless) -------- */
    @PostMapping("/logout")
    public ServiceResult<Void> logout() {
        return ServiceResult.noContent();                   // 204 No Content
    }

    /* -------- Utilidad para IP -------- */
    private String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        return (xfHeader != null && !xfHeader.isBlank())
                ? xfHeader.split(",")[0]
                : request.getRemoteAddr();
    }
}
