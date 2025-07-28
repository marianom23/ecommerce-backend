package com.empresa.ecommerce_backend.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.SignedJWT;


import java.net.URL;


//⚙️ Servicio central para:
//
//generateToken(): crear un token nuevo al hacer login (tradicional u OAuth2)
//
//isTokenValid(): revisar si un token recibido es válido y no expirado
//
//getAuthentication(): extrae el username + roles desde el token y construye un Authentication listo para Spring Security
//
//📌 Es quien sabe firmar, verificar y extraer información de tokens.

@Service
public class JwtService {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.azure.client-id}")
    private String azureClientId;

    /**
     * Genera un token JWT con username y roles.
     */
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();

        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Verifica si un token es válido.
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(jwtSecret.getBytes())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extrae el Authentication de un token JWT válido.
     */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject();

        List<String> roles = claims.get("roles", List.class);

        Collection<GrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }

    /**
     * Extrae el token del header Authorization.
     */
    public String extractTokenFromHeader(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    // JwtService.java
    public String generateEmailVerificationToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 1000 * 60 * 60 * 24); // 24h

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String validateEmailVerificationToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject(); // Email
        } catch (JwtException e) {
            return null;
        }
    }

    public boolean verifyIdToken(String idToken, String provider) {
        try {
            String issuer = switch (provider.toLowerCase()) {
                case "google" -> "https://accounts.google.com";
                case "azure-ad" -> "https://login.microsoftonline.com/common/v2.0";
                default -> throw new RuntimeException("Proveedor no soportado");
            };

            String jwksUri = switch (provider.toLowerCase()) {
                case "google" -> "https://www.googleapis.com/oauth2/v3/certs";
                case "azure-ad" -> "https://login.microsoftonline.com/common/discovery/v2.0/keys";
                default -> throw new RuntimeException("Proveedor no soportado");
            };

            String expectedAudience = switch (provider.toLowerCase()) {
                case "google" -> googleClientId;
                case "azure-ad" -> azureClientId;
                default -> throw new RuntimeException("Proveedor no soportado para audience");
            };

            // Cargar claves públicas
            JWKSource<SecurityContext> jwkSource = new RemoteJWKSet<>(new URL(jwksUri));
            ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
            jwtProcessor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource));

            JWTClaimsSet claimsSet = jwtProcessor.process(idToken, null);

            // Validar issuer
            if (!claimsSet.getIssuer().equals(issuer)) return false;

            // ✅ Validar audience (aud)
            List<String> audience = claimsSet.getAudience();
            if (audience == null || !audience.contains(expectedAudience)) {
                System.err.println("Audiencia inválida: " + audience);
                return false;
            }

            // Validar expiración
            Date now = new Date();
            if (claimsSet.getExpirationTime() == null || now.after(claimsSet.getExpirationTime())) return false;

            return true;

        } catch (Exception e) {
            System.err.println("Error verificando ID token: " + e.getMessage());
            return false;
        }
    }



}