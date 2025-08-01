package com.empresa.ecommerce_backend.service.interfaces;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.security.core.Authentication;

import java.net.URL;
import java.time.Instant;
public interface JwtService {

    String generateToken(Authentication authentication);

    boolean isTokenValid(String token);

    Authentication getAuthentication(String token);

    String extractTokenFromHeader(String header);

    String generateEmailVerificationToken(String email);

    String validateEmailVerificationToken(String token);

    boolean verifyIdToken(String idToken, String provider);

    Instant getExpiration(String token);

    String extractSub(String idToken, String provider);

}