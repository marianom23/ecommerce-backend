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
import com.empresa.ecommerce_backend.service.interfaces.JwtService;
import com.empresa.ecommerce_backend.web.CartCookieManager;
import com.empresa.ecommerce_backend.web.RefreshTokenCookieManager;
import com.empresa.ecommerce_backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UserServiceImpl userServiceImpl;
    private final CartCookieManager cartCookieManager;
    private final OAuth2UserProcessor oAuth2UserProcessor;
    private final RefreshTokenCookieManager refreshTokenCookieManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final com.empresa.ecommerce_backend.service.interfaces.CartService cartService;

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

        // Si login exitoso, generar access + refresh tokens Y fusionar carrito
        if (result.getData() != null
                && result.getStatus() != null
                && result.getStatus().is2xxSuccessful()) {
            
            LoginResponse data = result.getData();
            
            // 🛒 FUSIONAR CARRITO: Leer sessionId de header o cookie
            String guestSessionId = servletRequest.getHeader("X-Cart-Session");
            if (guestSessionId == null || guestSessionId.isBlank()) {
                // Fallback a cookie si no hay header
                jakarta.servlet.http.Cookie[] cookies = servletRequest.getCookies();
                if (cookies != null) {
                    for (jakarta.servlet.http.Cookie cookie : cookies) {
                        if ("cart_session".equals(cookie.getName())) {
                            guestSessionId = cookie.getValue();
                            break;
                        }
                    }
                }
            }
            
            // Fusionar carrito guest con usuario
            if (guestSessionId != null && !guestSessionId.isBlank()) {
                cartService.attachCartToUser(guestSessionId, data.getId());
            }
            
            // Generar access token (15 min)
            String accessToken = jwtService.generateAccessToken(data.getId(), data.getEmail(), data.getRoles());
            
            // Generar refresh token (7 días) y guardarlo en cookie
            String refreshToken = jwtService.generateRefreshToken(data.getId());
            refreshTokenCookieManager.setRefreshCookie(response, refreshToken, servletRequest.isSecure());
            
            // Reemplazar token en response con access token
            data.setToken(accessToken);
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

        // OAuth también usa dual-token Y fusión de carrito
        if (result.getData() != null
                && result.getStatus() != null
                && result.getStatus().is2xxSuccessful()) {
            
            LoginResponse data = result.getData();
            
            // 🛒 FUSIONAR CARRITO: Misma lógica que login normal
            String guestSessionId = servletRequest.getHeader("X-Cart-Session");
            if (guestSessionId == null || guestSessionId.isBlank()) {
                jakarta.servlet.http.Cookie[] cookies = servletRequest.getCookies();
                if (cookies != null) {
                    for (jakarta.servlet.http.Cookie cookie : cookies) {
                        if ("cart_session".equals(cookie.getName())) {
                            guestSessionId = cookie.getValue();
                            break;
                        }
                    }
                }
            }
            
            if (guestSessionId != null && !guestSessionId.isBlank()) {
                cartService.attachCartToUser(guestSessionId, data.getId());
            }
            
            // Generar access token (15 min)
            String accessToken = jwtService.generateAccessToken(data.getId(), data.getEmail(), data.getRoles());
            
            // Generar refresh token (7 días)
            String refreshToken = jwtService.generateRefreshToken(data.getId());
            refreshTokenCookieManager.setRefreshCookie(response, refreshToken, servletRequest.isSecure());
            
            // Reemplazar con access token
            data.setToken(accessToken);
        }

        return result;
    }

    /* -------- Perfil -------- */
    @GetMapping("/me")
    public ServiceResult<UserMeResponse> getProfile(Authentication authentication) {
        return userServiceImpl.getProfile(authentication);
    }

    @PutMapping("/me/profile")
    public ServiceResult<UserMeResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody com.empresa.ecommerce_backend.dto.request.UpdateProfileRequest dto
    ) {
        Long userId = getUserIdFromAuth(authentication);
        return userServiceImpl.updateProfile(userId, dto);
    }

    @PostMapping("/me/password")
    public ServiceResult<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody com.empresa.ecommerce_backend.dto.request.ChangePasswordRequest dto
    ) {
        Long userId = getUserIdFromAuth(authentication);
        return userServiceImpl.changePassword(userId, dto);
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("No autenticado");
        }
        try {
            // Asumiendo que el principal es AuthUser o tiene getId()
            return (Long) authentication.getPrincipal().getClass().getMethod("getId").invoke(authentication.getPrincipal());
        } catch (Exception e) {
            // Fallback si es solo ID numérico
            try {
                return Long.parseLong(authentication.getName());
            } catch (NumberFormatException ex) {
                throw new IllegalStateException("No se pudo obtener ID de usuario");
            }
        }
    }

    /* -------- Refresh Token -------- */
    @PostMapping("/auth/refresh")
    public ServiceResult<com.empresa.ecommerce_backend.dto.response.TokenResponse> refreshToken(
            @CookieValue(value = "refresh_token", required = false) String refreshToken
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ServiceResult.error(org.springframework.http.HttpStatus.UNAUTHORIZED, "No refresh token");
        }

        if (!jwtService.isValidRefreshToken(refreshToken)) {
            return ServiceResult.error(org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        // Extraer userId y generar nuevo access token
        Long userId = jwtService.getUserIdFromToken(refreshToken);
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .toList();

        String newAccessToken = jwtService.generateAccessToken(userId, user.getEmail(), roles);

        return ServiceResult.ok(new com.empresa.ecommerce_backend.dto.response.TokenResponse(newAccessToken));
    }

    /* -------- Logout -------- */
    @PostMapping("/logout")
    public ServiceResult<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        // Limpiamos carrito + refresh token
        cartCookieManager.clearSessionCookie(response, request.isSecure());
        refreshTokenCookieManager.clearRefreshCookie(response, request.isSecure());
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
