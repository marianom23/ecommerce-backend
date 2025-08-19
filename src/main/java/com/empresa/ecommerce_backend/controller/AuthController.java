package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.request.LoginRequest;
import com.empresa.ecommerce_backend.dto.request.OAuthCallbackRequest;
import com.empresa.ecommerce_backend.dto.request.RegisterUserRequest;
import com.empresa.ecommerce_backend.dto.response.LoginResponse;
import com.empresa.ecommerce_backend.dto.response.RegisterUserResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.service.UserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UserServiceImpl userServiceImpl;

    /* -------- Registro -------- */
    @PostMapping("/register")
    @Operation(
            summary = "Registrar un nuevo usuario",
            description = "Este endpoint permite registrar un nuevo usuario proporcionando los datos necesarios."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario registrado con éxito"),
            @ApiResponse(responseCode = "400", description = "Datos incorrectos o incompletos"),
            @ApiResponse(responseCode = "409", description = "El usuario ya existe")
    })
    public ServiceResult<RegisterUserResponse> register(
            @Valid @RequestBody
            @Parameter(description = "Datos del nuevo usuario para el registro", required = true)
            RegisterUserRequest dto) {
        return userServiceImpl.registerUser(dto);           // devuelve created(...)
    }

    /* -------- Verificación de correo -------- */
    @GetMapping("/verify-email")
    @Operation(
            summary = "Verificar correo electrónico",
            description = "Verifica la validez del token de verificación de correo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Correo electrónico verificado correctamente"),
            @ApiResponse(responseCode = "400", description = "Token inválido o caducado")
    })
    public ServiceResult<Void> verifyEmail(
            @RequestParam("token")
            @Parameter(description = "Token de verificación enviado al correo electrónico", required = true)
            String token) {
        return userServiceImpl.verifyEmail(token);          // ok() o error(...)
    }

    /* -------- Login -------- */
    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = "Este endpoint permite a un usuario iniciar sesión utilizando sus credenciales."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    public ServiceResult<?> login(
            @Valid @RequestBody
            @Parameter(description = "Credenciales de login del usuario", required = true)
            LoginRequest request,
            HttpServletRequest servletRequest) {
        String ip = extractClientIp(servletRequest);
        return userServiceImpl.login(request, ip);          // ok()
    }

    /* -------- Callback OAuth2 (Google / Azure AD) -------- */
    @PostMapping("/oauth2/callback")
    @Operation(
            summary = "Callback de OAuth2",
            description = "Este endpoint maneja el callback después de un login OAuth2 exitoso (Google / Azure AD)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Callback procesado correctamente"),
            @ApiResponse(responseCode = "400", description = "Error en el callback")
    })
    public ServiceResult<LoginResponse> handleOAuthCallback(
            @RequestBody
            @Parameter(description = "Datos del callback OAuth2", required = true)
            OAuthCallbackRequest dto,
            HttpServletRequest servletRequest) {
        String ip = extractClientIp(servletRequest);
        return userServiceImpl.handleOAuthCallback(dto, ip); // ok()
    }

    /* -------- Perfil del usuario autenticado -------- */
    @GetMapping("/me")
    @Operation(
            summary = "Obtener perfil de usuario",
            description = "Este endpoint devuelve el perfil del usuario autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil obtenido correctamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    public UserDetails getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return userDetails;                                 // 200 OK plano
    }

    /* -------- Logout (stateless) -------- */
    @PostMapping("/logout")
    @Operation(
            summary = "Cerrar sesión",
            description = "Este endpoint permite cerrar la sesión del usuario en el sistema."
    )
    @ApiResponse(responseCode = "204", description = "Logout exitoso")
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
