// src/main/java/com/empresa/ecommerce_backend/service/UserServiceImpl.java
package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.LoginRequest;
import com.empresa.ecommerce_backend.dto.request.OAuthCallbackRequest;
import com.empresa.ecommerce_backend.dto.request.RegisterUserRequest;
import com.empresa.ecommerce_backend.dto.response.LoginResponse;
import com.empresa.ecommerce_backend.dto.response.RegisterUserResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.dto.response.UserMeResponse;
import com.empresa.ecommerce_backend.enums.AuthProvider;
import com.empresa.ecommerce_backend.enums.RoleName;
import com.empresa.ecommerce_backend.events.UserRegisteredEvent;
import com.empresa.ecommerce_backend.mapper.UserMapper;
import com.empresa.ecommerce_backend.model.Role;
import com.empresa.ecommerce_backend.model.User;
import com.empresa.ecommerce_backend.repository.RoleRepository;
import com.empresa.ecommerce_backend.repository.UserRepository;
import com.empresa.ecommerce_backend.security.AuthUser;
import com.empresa.ecommerce_backend.service.interfaces.JwtService;
import com.empresa.ecommerce_backend.service.interfaces.LoginAttemptService;
import com.empresa.ecommerce_backend.service.interfaces.MailService;
import com.empresa.ecommerce_backend.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MailService mailService;
    private final UserMapper userMapper;
    private final LoginAttemptService loginAttemptService;
    private final ApplicationEventPublisher publisher;

    /* =================== REGISTER =================== */
    @Transactional
    public ServiceResult<RegisterUserResponse> registerUser(RegisterUserRequest dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            return ServiceResult.error(HttpStatus.CONFLICT, "El email ya está registrado.");
        }

        try {
            User user = userMapper.toEntity(dto);
            user.setVerified(false);
            user.setAuthProvider(AuthProvider.LOCAL);

            Role defaultRole = roleRepository.findByName(RoleName.CUSTOMER)
                    .orElseThrow(() -> new IllegalStateException("Rol CUSTOMER no encontrado"));
            user.getRoles().add(defaultRole);
            user.setPassword(passwordEncoder.encode(dto.getPassword()));

            User saved = userRepository.save(user);

            String token = jwtService.generateEmailVerificationToken(saved.getEmail());
            publisher.publishEvent(new UserRegisteredEvent(saved.getEmail(), token));

            RegisterUserResponse response = userMapper.toRegisterResponse(saved);
            return ServiceResult.created(response);

        } catch (DataIntegrityViolationException e) {
            return ServiceResult.error(HttpStatus.CONFLICT, "El email ya está registrado.");
        }
    }

    /* =================== VERIFY EMAIL =================== */
    public ServiceResult<Void> verifyEmail(String token) {
        String email = jwtService.validateEmailVerificationToken(token);
        if (email == null) {
            return ServiceResult.error(HttpStatus.BAD_REQUEST, "Token inválido o expirado.");
        }

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ServiceResult.error(HttpStatus.NOT_FOUND, "Usuario no encontrado.");
        }

        User user = optionalUser.get();
        if (user.isVerified()) {
            return new ServiceResult<>("La cuenta ya estaba verificada.", null, HttpStatus.OK);
        }

        user.setVerified(true);
        userRepository.save(user);
        return new ServiceResult<>("Cuenta verificada exitosamente.", null, HttpStatus.OK);
    }

    /* =================== LOGIN =================== */
    public ServiceResult<LoginResponse> login(LoginRequest request, String ip) {
        try {
            Optional<User> optionalUser = userRepository.findByEmail(request.email());

            if (optionalUser.isEmpty()) {
                loginAttemptService.logAttempt(null, ip, false, "Usuario no encontrado.");
                return ServiceResult.error(HttpStatus.UNAUTHORIZED, "Credenciales inválidas.");
            }

            User user = optionalUser.get();

            // Cuenta creada solo por OAuth (sin password local)
            if (user.getPassword() == null || user.getPassword().isBlank()) {
                loginAttemptService.logAttempt(user, ip, false, "Sin contraseña (OAuth).");
                return ServiceResult.error(
                        HttpStatus.BAD_REQUEST,
                        "Este usuario no tiene contraseña configurada. Inicie sesión con Google o Microsoft."
                );
            }

            if (!passwordEncoder.matches(request.password(), user.getPassword())) {
                loginAttemptService.logAttempt(user, ip, false, "Credenciales inválidas.");
                return ServiceResult.error(HttpStatus.UNAUTHORIZED, "Credenciales inválidas.");
            }

            // Si quisieras exigir email verificado, lo descomentas:
            // if (!user.isVerified()) {
            //     loginAttemptService.logAttempt(user, ip, false, "Email no verificado.");
            //     return ServiceResult.error(HttpStatus.FORBIDDEN, "Debes verificar tu email.");
            // }

            Authentication authentication = buildAuthenticationFromUser(user);

            loginAttemptService.logAttempt(user, ip, true, null);

            String token = jwtService.generateToken(authentication, user.getId());
            Instant expiresAt = jwtService.getExpiration(token);

            LoginResponse loginResponse = userMapper.toLoginResponse(user, token, expiresAt);
            return ServiceResult.ok(loginResponse);

        } catch (BadCredentialsException e) {
            userRepository.findByEmail(request.email())
                    .ifPresentOrElse(
                            u -> loginAttemptService.logAttempt(u, ip, false, "Credenciales inválidas."),
                            () -> loginAttemptService.logAttempt(null, ip, false, "Credenciales inválidas.")
                    );
            return ServiceResult.error(HttpStatus.UNAUTHORIZED, "Credenciales inválidas.");

        } catch (Exception e) {
            loginAttemptService.logAttempt(null, ip, false, "Error de autenticación.");
            return ServiceResult.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error de autenticación.");
        }
    }


    /* -------- Perfil (/api/me) -------- */
    public ServiceResult<UserMeResponse> getProfile(Authentication authentication) {
        try {
            log.info("🔎 /me -> authentication = {}, principal = {}",
                    authentication,
                    authentication != null ? authentication.getPrincipal().getClass().getName() : "null");

            User user = getUserFromAuthentication(authentication);
            UserMeResponse dto = userMapper.toMeResponse(user); // o toUserMeResponse, según tu mapper
            return ServiceResult.ok(dto);

        } catch (UsernameNotFoundException e) {
            return ServiceResult.error(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        } catch (Exception e) {
            log.error("Error en getProfile", e);
            return ServiceResult.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error al obtener el perfil");
        }
    }

    private User getUserFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("No autenticado");
        }

        Object principal = authentication.getPrincipal();

        // ✅ CASO 1: principal = AuthUser (lo que muestran tus logs)
        if (principal instanceof AuthUser authUser) {
            Long id = authUser.getId();
            return userRepository.findById(id)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        }

        // ✅ CASO 2: por si en algún flujo el principal es solo el id (Long)
        if (principal instanceof Long l) {
            return userRepository.findById(l)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        }

        // ✅ CASO 3: por si el principal es el id en String ("2", "3", etc.)
        if (principal instanceof String s) {
            try {
                Long id = Long.parseLong(s);
                return userRepository.findById(id)
                        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
            } catch (NumberFormatException e) {
                throw new UsernameNotFoundException("Principal inválido");
            }
        }

        // ❌ Cualquier otra cosa, no sabemos resolverla
        throw new UsernameNotFoundException(
                "Tipo de principal no soportado: " + principal.getClass().getName()
        );
    }




    /* =================== HELPERS =================== */

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
