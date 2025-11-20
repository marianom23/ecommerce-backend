package com.empresa.ecommerce_backend.service.interfaces;

import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.Map;

public interface JwtService {

    String generateToken(Authentication authentication, Long userId);

    boolean isTokenValid(String token);

    Authentication getAuthentication(String token);

    String extractTokenFromHeader(String header);

    String generateEmailVerificationToken(String email);

    String validateEmailVerificationToken(String token);

    boolean verifyIdToken(String idToken, String provider);

    Instant getExpiration(String token);

    String extractSub(String idToken, String provider);

    /* ===== NUEVOS MÉTODOS PARA ID_TOKEN (GOOGLE / AZURE) ===== */

    /**
     * Devuelve todos los claims del id_token como un Map.
     */
    Map<String, Object> extractClaims(String idToken);

    /**
     * Devuelve el email del id_token (claim "email").
     */
    String extractEmail(String idToken);

    /**
     * Devuelve el nombre (given_name o derivado de "name").
     */
    String extractFirstName(String idToken);

    /**
     * Devuelve el apellido (family_name o derivado de "name").
     */
    String extractLastName(String idToken);
}
