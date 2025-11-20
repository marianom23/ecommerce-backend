// src/main/java/com/empresa/ecommerce_backend/service/OAuth2UserProcessor.java
package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.OAuthCallbackRequest;
import com.empresa.ecommerce_backend.dto.response.LoginResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.enums.AuthProvider;
import com.empresa.ecommerce_backend.enums.RoleName;
import com.empresa.ecommerce_backend.mapper.UserMapper;
import com.empresa.ecommerce_backend.model.Role;
import com.empresa.ecommerce_backend.model.User;
import com.empresa.ecommerce_backend.repository.RoleRepository;
import com.empresa.ecommerce_backend.repository.UserRepository;
import com.empresa.ecommerce_backend.service.interfaces.JwtService;
import com.empresa.ecommerce_backend.service.interfaces.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserProcessor {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final UserMapper userMapper;

    /* ========= Escenario 1: Spring Security oauth2Login + SuccessHandler ========= */
    public ServiceResult<LoginResponse> processOAuth2User(
            OAuth2User oauthUser,
            String registrationId,
            String ip
    ) {
        try {
            String provider = registrationId.toLowerCase();
            AuthProvider providerEnum = mapProvider(provider);

            // Normalmente oauthUser.getName() viene del "sub" del proveedor
            String oauthId = oauthUser.getName();

            Map<String, Object> attrs = oauthUser.getAttributes();
            String email = (String) attrs.getOrDefault("email", null);
            String firstName = (String) (
                    attrs.getOrDefault("given_name",
                            attrs.getOrDefault("givenName", null))
            );
            String lastName = (String) (
                    attrs.getOrDefault("family_name",
                            attrs.getOrDefault("familyName", null))
            );

            User user = upsertOAuthUser(oauthId, email, firstName, lastName, providerEnum);

            Authentication auth = buildAuthenticationFromUser(user);
            String jwt = jwtService.generateToken(auth, user.getId());
            Instant expiresAt = jwtService.getExpiration(jwt);

            loginAttemptService.logAttempt(user, ip, true, null);

            LoginResponse loginResponse = userMapper.toLoginResponse(user, jwt, expiresAt);
            return ServiceResult.ok(loginResponse);

        } catch (Exception e) {
            log.error("Error procesando usuario OAuth2", e);
            loginAttemptService.logAttempt(null, ip, false, "Error al procesar usuario OAuth2");
            return ServiceResult.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error al procesar usuario OAuth2");
        }
    }

    /* ========= Escenario 2: Frontend manda id_token a /api/oauth2/callback ========= */
    public ServiceResult<LoginResponse> processFromFrontendOAuthCallback(
            OAuthCallbackRequest dto,
            String ip
    ) {
        try {
            String provider = dto.getProvider();
            AuthProvider providerEnum = mapProvider(provider);

            // 1) Validar el id_token contra Google/Microsoft
            boolean valid = jwtService.verifyIdToken(dto.getIdToken(), provider);
            if (!valid) {
                loginAttemptService.logAttempt(null, ip, false, "ID token inválido");
                return ServiceResult.error(HttpStatus.UNAUTHORIZED, "ID token inválido");
            }

            // 2) Extraer "sub" = oauthId
            String oauthId = jwtService.extractSub(dto.getIdToken(), provider);
            if (oauthId == null) {
                loginAttemptService.logAttempt(null, ip, false, "No se pudo extraer el sub");
                return ServiceResult.error(HttpStatus.UNAUTHORIZED, "Token inválido");
            }

            // 3) Completar claims: email / nombre / apellido
            String email = Optional.ofNullable(dto.getEmail())
                    .filter(s -> !s.isBlank())
                    .orElse(jwtService.extractEmail(dto.getIdToken()));

            String firstName = Optional.ofNullable(dto.getFirstName())
                    .filter(s -> !s.isBlank())
                    .orElse(jwtService.extractFirstName(dto.getIdToken()));

            String lastName = Optional.ofNullable(dto.getLastName())
                    .filter(s -> !s.isBlank())
                    .orElse(jwtService.extractLastName(dto.getIdToken()));

            // 4) Buscar o crear usuario
            User user = upsertOAuthUser(oauthId, email, firstName, lastName, providerEnum);

            // 5) Construir Authentication + JWT
            Authentication auth = buildAuthenticationFromUser(user);
            String jwt = jwtService.generateToken(auth, user.getId());
            Instant expiresAt = jwtService.getExpiration(jwt);

            loginAttemptService.logAttempt(user, ip, true, null);

            LoginResponse loginResponse = userMapper.toLoginResponse(user, jwt, expiresAt);
            return ServiceResult.ok(loginResponse);

        } catch (Exception e) {
            log.error("Error al verificar ID token", e);
            loginAttemptService.logAttempt(null, ip, false, "Error al verificar ID token");
            return ServiceResult.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error al verificar ID token");
        }
    }

    /* ========= Helpers compartidos ========= */

    private User upsertOAuthUser(
            String oauthId,
            String email,
            String firstName,
            String lastName,
            AuthProvider providerEnum
    ) {
        // Los parámetros son efectivamente final -> no rompe el lambda
        return userRepository.findByOauthId(oauthId).orElseGet(() -> {
            Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                    .orElseThrow(() -> new IllegalStateException("Rol CUSTOMER no encontrado"));

            User u = new User();
            u.setOauthId(oauthId);
            u.setEmail(email);
            u.setFirstName(firstName != null ? firstName : "Usuario");
            u.setLastName(lastName != null ? lastName : "OAuth");
            u.setVerified(true);
            u.setAuthProvider(providerEnum);
            u.setRoles(Set.of(customerRole));
            return userRepository.save(u);
        });
    }

    private AuthProvider mapProvider(String provider) {
        return switch (provider.toLowerCase()) {
            case "google" -> AuthProvider.GOOGLE;
            case "azure-ad", "azuread", "microsoft" -> AuthProvider.AZURE_AD;
            default -> AuthProvider.LOCAL;
        };
    }

    private Authentication buildAuthenticationFromUser(User user) {
        return new UsernamePasswordAuthenticationToken(
                user.getId().toString(),
                null,
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(
                                role.getName().name().startsWith("ROLE_")
                                        ? role.getName().name()
                                        : "ROLE_" + role.getName().name()
                        ))
                        .collect(Collectors.toList())
        );
    }
}
