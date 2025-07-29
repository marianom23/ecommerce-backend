package com.empresa.ecommerce_backend.service.interfaces;

import org.springframework.security.core.Authentication;

public interface JwtService {

    String generateToken(Authentication authentication);

    boolean isTokenValid(String token);

    Authentication getAuthentication(String token);

    String extractTokenFromHeader(String header);

    String generateEmailVerificationToken(String email);

    String validateEmailVerificationToken(String token);

    boolean verifyIdToken(String idToken, String provider);
}