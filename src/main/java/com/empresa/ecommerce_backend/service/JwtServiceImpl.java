package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.security.AuthUser;
import com.empresa.ecommerce_backend.service.interfaces.JwtService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;


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
public class JwtServiceImpl implements JwtService {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${AZURE_CLIENT_ID}")
    private String azureClientId;

    @Value("${AZURE_AD_TENANT_ID}")
    private String azureTenantId;

    public Map<String, Object> extractClaims(String idToken) {
        try {
            SignedJWT jwt = SignedJWT.parse(idToken);
            return jwt.getJWTClaimsSet().getClaims();
        } catch (Exception e) {
            return Map.of();
        }
    }

    public String extractEmail(String idToken) {
        try {
            SignedJWT jwt = SignedJWT.parse(idToken);
            return jwt.getJWTClaimsSet().getStringClaim("email");
        } catch (Exception e) {
            return null;
        }
    }

    public String extractFirstName(String idToken) {
        try {
            SignedJWT jwt = SignedJWT.parse(idToken);

            // Google: given_name
            // Azure AD: name (a veces), given_name
            String given = jwt.getJWTClaimsSet().getStringClaim("given_name");
            if (given != null && !given.isBlank()) return given;

            String name = jwt.getJWTClaimsSet().getStringClaim("name");
            if (name != null && name.contains(" ")) return name.split(" ")[0];

            return null;

        } catch (Exception e) {
            return null;
        }
    }

    public String extractLastName(String idToken) {
        try {
            SignedJWT jwt = SignedJWT.parse(idToken);

            // Google: family_name
            // Azure AD: name (a veces), family_name
            String family = jwt.getJWTClaimsSet().getStringClaim("family_name");
            if (family != null && !family.isBlank()) return family;

            String name = jwt.getJWTClaimsSet().getStringClaim("name");
            if (name != null && name.contains(" ")) {
                return String.join(" ", Arrays.copyOfRange(name.split(" "), 1, name.split(" ").length));
            }

            return null;

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String generateToken(Authentication authentication, Long userId) {
        String username = authentication.getName();

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        JwtBuilder builder = Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256);

        if (userId != null) builder.claim("uid", userId); // 👈 guardamos el id
        return builder.compact();
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


    @Override
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject();

        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        if (roles == null) roles = List.of();

        Collection<GrantedAuthority> authorities = roles.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(r -> !r.isEmpty())
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        Long uid = null;
        Object rawUid = claims.get("uid");
        if (rawUid instanceof Number n) uid = n.longValue();
        else if (rawUid instanceof String s && !s.isBlank()) uid = Long.parseLong(s);

        var principal = new AuthUser(uid, username, "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
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
                case "azure-ad" -> "https://login.microsoftonline.com/" + azureTenantId + "/v2.0";
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

            JWKSource<SecurityContext> jwkSource = new RemoteJWKSet<>(new URL(jwksUri));
            ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
            jwtProcessor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource));

            JWTClaimsSet claimsSet = jwtProcessor.process(idToken, null);

            if (!claimsSet.getIssuer().equals(issuer)) return false;

            List<String> audience = claimsSet.getAudience();
            if (audience == null || !audience.contains(expectedAudience)) return false;

            Date now = new Date();
            if (claimsSet.getExpirationTime() == null || now.after(claimsSet.getExpirationTime())) return false;

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Instant getExpiration(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();

        Date exp = claims.getExpiration();
        return exp != null ? exp.toInstant() : null;
    }

    public String extractSub(String idToken, String provider) {
        try {
            String jwksUri = switch (provider.toLowerCase()) {
                case "google" -> "https://www.googleapis.com/oauth2/v3/certs";
                case "azure-ad" -> "https://login.microsoftonline.com/common/discovery/v2.0/keys";
                default -> throw new RuntimeException("Proveedor no soportado");
            };

            JWKSource<SecurityContext> jwkSource = new RemoteJWKSet<>(new URL(jwksUri));
            ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
            jwtProcessor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource));
            JWTClaimsSet claimsSet = jwtProcessor.process(idToken, null);

            return claimsSet.getSubject(); // sub

        } catch (Exception e) {
            return null;
        }
    }

    /* ===== REFRESH TOKEN IMPLEMENTATION ===== */

    @Override
    public String generateAccessToken(Long userId, String username, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 900000); // 15 minutos
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .setSubject(username)
                .claim("uid", userId)
                .claim("roles", roles)
                .claim("type", "access")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 604800000); // 7 dias
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("type", "refresh")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public boolean isValidRefreshToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return "refresh".equals(claims.get("type"));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public Long getUserIdFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String sub = claims.getSubject();
            return Long.parseLong(sub);
        } catch (Exception e) {
            return null;
        }
    }


}