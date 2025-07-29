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
    public ResponseEntity<?> register(@RequestBody @Valid RegisterUserRequest dto) {
        ServiceResult<RegisterUserResponse> result = userServiceImpl.registerUser(dto);
        if (result.isSuccess()) return ResponseEntity.ok(result.getData());
        return ResponseEntity.badRequest().body(result.getMessage());
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        var result = userServiceImpl.verifyEmail(token);
        if (result.isSuccess()) return ResponseEntity.ok().build();
        return ResponseEntity.badRequest().body(result.getMessage());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request, HttpServletRequest servletRequest) {
        String ip = extractClientIp(servletRequest);
        var result = userServiceImpl.login(request, ip);
        if (result.isSuccess()) return ResponseEntity.ok(result.getData());
        return ResponseEntity.badRequest().body(result.getMessage());
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

    @PostMapping("/oauth2/callback")
    public ResponseEntity<?> handleOAuthCallback(@RequestBody OAuthCallbackRequest dto, HttpServletRequest servletRequest) {
        String ip = extractClientIp(servletRequest);
        var result = userServiceImpl.handleOAuthCallback(dto, ip);
        if (result.isSuccess()) return ResponseEntity.ok(result.getData());
        return ResponseEntity.badRequest().body(result.getMessage());
    }

    private String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isBlank()) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }

}
