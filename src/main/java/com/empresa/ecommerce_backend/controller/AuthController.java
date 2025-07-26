package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.request.LoginRequest;
import com.empresa.ecommerce_backend.dto.request.RegisterUserRequest;
import com.empresa.ecommerce_backend.dto.response.LoginResponse;
import com.empresa.ecommerce_backend.dto.response.RegisterUserResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterUserRequest dto) {
        ServiceResult<RegisterUserResponse> result = userService.registerUser(dto);
        if (result.isSuccess()) return ResponseEntity.ok(result.getData());
        return ResponseEntity.badRequest().body(result.getMessage());
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        var result = userService.verifyEmail(token);
        if (result.isSuccess()) return ResponseEntity.ok().build();
        return ResponseEntity.badRequest().body(result.getMessage());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {
        var result = userService.login(request);
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

    @GetMapping("/oauth2/success")
    public ResponseEntity<?> oauth2Success(@RequestParam("token") String jwt) {
        return ResponseEntity.ok().body("{\"token\": \"" + jwt + "\"}");
    }
}
