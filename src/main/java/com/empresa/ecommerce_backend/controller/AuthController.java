package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.request.LoginRequest;
import com.empresa.ecommerce_backend.dto.request.RegisterUserRequest;
import com.empresa.ecommerce_backend.dto.request.OAuthCallbackRequest;
import com.empresa.ecommerce_backend.dto.response.RegisterUserResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.service.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UserServiceImpl userServiceImpl;

    @PostMapping("/register")
    public ResponseEntity<ServiceResult<RegisterUserResponse>> register(@RequestBody @Valid RegisterUserRequest dto) {
        return ResponseEntity.ok(userServiceImpl.registerUser(dto));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<ServiceResult<Void>> verifyEmail(@RequestParam("token") String token) {
        return ResponseEntity.ok(userServiceImpl.verifyEmail(token));
    }

    @PostMapping("/login")
    public ResponseEntity<ServiceResult<?>> login(@RequestBody @Valid LoginRequest request, HttpServletRequest servletRequest) {
        String ip = extractClientIp(servletRequest);
        return ResponseEntity.ok(userServiceImpl.login(request, ip));
    }

    @PostMapping("/oauth2/callback")
    public ResponseEntity<ServiceResult<String>> handleOAuthCallback(@RequestBody OAuthCallbackRequest dto, HttpServletRequest servletRequest) {
        String ip = extractClientIp(servletRequest);
        return ResponseEntity.ok(userServiceImpl.handleOAuthCallback(dto, ip));
    }

    private String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isBlank()) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userDetails);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Stateless: frontend solo elimina el token. Este endpoint es opcional.
        return ResponseEntity.ok().build();
    }

}
