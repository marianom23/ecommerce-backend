// src/main/java/com/empresa/ecommerce_backend/controller/AuthController.java
package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.request.LoginRequest;
import com.empresa.ecommerce_backend.dto.request.OAuthCallbackRequest;
import com.empresa.ecommerce_backend.dto.request.RegisterUserRequest;
import com.empresa.ecommerce_backend.dto.response.LoginResponse;
import com.empresa.ecommerce_backend.dto.response.RegisterUserResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.dto.response.UserMeResponse;
import com.empresa.ecommerce_backend.service.OAuth2UserProcessor;
import com.empresa.ecommerce_backend.service.UserServiceImpl;
import com.empresa.ecommerce_backend.web.CartCookieManager;
import com.empresa.ecommerce_backend.web.AuthCookieManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UserServiceImpl userServiceImpl;
    private final CartCookieManager cartCookieManager;
    private final OAuth2UserProcessor oAuth2UserProcessor;
    private final AuthCookieManager authCookieManager;

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

    /* -------- Login (credenciales) -------- */
    @PostMapping("/login")
    public ServiceResult<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse response
    ) {
        String ip = extractClientIp(servletRequest);
        ServiceResult<LoginResponse> result = userServiceImpl.login(request, ip);

        // Si el login fue exitoso, setear cookie httpOnly con el JWT
        if (result.getData() != null
                && result.getStatus() != null
                && result.getStatus().is2xxSuccessful()) {
            String token = result.getData().getToken();
            authCookieManager.setAuthCookie(response, token, servletRequest.isSecure());
        }

        return result;
    }

    /* -------- Callback OAuth2 (flujo id_token desde el front) -------- */
    @PostMapping("/oauth2/callback")
    public ServiceResult<LoginResponse> handleOAuthCallback(
            @RequestBody OAuthCallbackRequest dto,
            HttpServletRequest servletRequest,
            HttpServletResponse response
    ) {
        String ip = extractClientIp(servletRequest);
        ServiceResult<LoginResponse> result =
                oAuth2UserProcessor.processFromFrontendOAuthCallback(dto, ip);

        // También acá seteamos cookie si todo salió bien
        if (result.getData() != null
                && result.getStatus() != null
                && result.getStatus().is2xxSuccessful()) {
            String token = result.getData().getToken();
            authCookieManager.setAuthCookie(response, token, servletRequest.isSecure());
        }

        return result;
    }

    /* -------- Perfil -------- */
    @GetMapping("/me")
    public ServiceResult<UserMeResponse> getProfile(Authentication authentication) {
        return userServiceImpl.getProfile(authentication);
    }

    /* -------- Logout -------- */
    @PostMapping("/logout")
    public ServiceResult<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        // Limpiamos carrito + auth
        cartCookieManager.clearSessionCookie(response, request.isSecure());
        authCookieManager.clearAuthCookie(response, request.isSecure());
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
