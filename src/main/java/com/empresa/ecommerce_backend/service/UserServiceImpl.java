// src/main/java/com/empresa/ecommerce_backend/service/UserServiceImpl.java
package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.LoginRequest;
import com.empresa.ecommerce_backend.dto.request.OAuthCallbackRequest;
import com.empresa.ecommerce_backend.dto.request.RegisterUserRequest;
import com.empresa.ecommerce_backend.dto.response.LoginResponse;
import com.empresa.ecommerce_backend.dto.response.RegisterUserResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.enums.AuthProvider;
import com.empresa.ecommerce_backend.enums.RoleName;
import com.empresa.ecommerce_backend.events.UserRegisteredEvent;
import com.empresa.ecommerce_backend.mapper.UserMapper;
import com.empresa.ecommerce_backend.model.Role;
import com.empresa.ecommerce_backend.model.User;
import com.empresa.ecommerce_backend.repository.RoleRepository;
import com.empresa.ecommerce_backend.repository.UserRepository;
import com.empresa.ecommerce_backend.service.interfaces.JwtService;
import com.empresa.ecommerce_backend.service.interfaces.LoginAttemptService;
import com.empresa.ecommerce_backend.service.interfaces.MailService;
import com.empresa.ecommerce_backend.service.interfaces.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        // Validación “rápida” para feedback inmediato
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

            // Generar token y publicar evento (el Listener lo enviará AFTER_COMMIT y en @Async)
            String token = jwtService.generateEmailVerificationToken(saved.getEmail());
            publisher.publishEvent(new UserRegisteredEvent(saved.getEmail(), token));

            RegisterUserResponse response = userMapper.toRegisterResponse(saved);
            return ServiceResult.created(response); // 201 Created

        } catch (DataIntegrityViolationException e) {
            // Por si dos requests pasan el existsByEmail al mismo tiempo (índice único en BD)
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
            // Idempotente: ya estaba verificada → 200 OK con mensaje
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
                // Evitar user enumeration → devolver 401 genérico
                loginAttemptService.logAttempt(null, ip, false, "Usuario no encontrado.");
                return ServiceResult.error(HttpStatus.UNAUTHORIZED, "Credenciales inválidas.");
            }

            User user = optionalUser.get();

            if (user.getPassword() == null || user.getPassword().isBlank()) {
                loginAttemptService.logAttempt(user, ip, false, "Sin contraseña (OAuth).");
                return ServiceResult.error(
                        HttpStatus.BAD_REQUEST,
                        "Este usuario no tiene contraseña configurada. Inicie sesión con Google o Microsoft."
                );
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );

            String token = jwtService.generateToken(authentication);

            loginAttemptService.logAttempt(user, ip, true, null);

            LoginResponse loginResponse = userMapper.toLoginResponse(user, token);
            return ServiceResult.ok(loginResponse); // 200 OK

        } catch (BadCredentialsException e) {
            userRepository.findByEmail(request.email())
                    .ifPresentOrElse(
                            user -> loginAttemptService.logAttempt(user, ip, false, "Credenciales inválidas."),
                            () -> loginAttemptService.logAttempt(null, ip, false, "Credenciales inválidas.")
                    );
            return ServiceResult.error(HttpStatus.UNAUTHORIZED, "Credenciales inválidas.");

        } catch (Exception e) {
            loginAttemptService.logAttempt(null, ip, false, "Error de autenticación.");
            return ServiceResult.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error de autenticación.");
        }
    }

    /* =================== OAUTH CALLBACK =================== */
    public ServiceResult<String> handleOAuthCallback(OAuthCallbackRequest dto, String ip) {
        try {
            boolean tokenValido = jwtService.verifyIdToken(dto.getIdToken(), dto.getProvider());
            if (!tokenValido) {
                loginAttemptService.logAttempt(null, ip, false, "ID token inválido");
                return ServiceResult.error(HttpStatus.UNAUTHORIZED, "ID token inválido");
            }

            User user = userRepository.findByEmail(dto.getEmail()).orElseGet(() -> {
                Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                        .orElseThrow(() -> new RuntimeException("Rol CUSTOMER no encontrado"));

                User nuevo = userMapper.fromOAuthDto(dto);
                // ▶ Cuenta creada vía OAuth: considérala verificada
                nuevo.setVerified(true);
                nuevo.setAuthProvider(AuthProvider.valueOf(dto.getProvider().toUpperCase().replace("-", "_")));
                nuevo.setRoles(Set.of(customerRole));
                // (sin password)
                return userRepository.save(nuevo);
            });

            // ▶ Si el usuario ya existía y aún no estaba verificado, márcalo como verificado ahora.
            if (!user.isVerified()) {
                user.setVerified(true);
                userRepository.save(user);
            }

            // Si no tenía provider, guardalo (no pisamos si ya tiene uno)
            if (user.getAuthProvider() == null) {
                user.setAuthProvider(AuthProvider.valueOf(dto.getProvider().toUpperCase().replace("-", "_")));
                userRepository.save(user);
            }

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    user.getEmail(),
                    null,
                    user.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                            .collect(Collectors.toList())
            );

            String jwt = jwtService.generateToken(auth);

            loginAttemptService.logAttempt(user, ip, true, null);
            return ServiceResult.ok(jwt); // 200 OK

        } catch (Exception e) {
            loginAttemptService.logAttempt(null, ip, false, "Error al verificar ID token");
            return ServiceResult.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error al verificar ID token");
        }
    }

}
