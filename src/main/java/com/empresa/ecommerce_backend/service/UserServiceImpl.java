package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.*;
import com.empresa.ecommerce_backend.dto.response.*;
import com.empresa.ecommerce_backend.enums.RoleName;
import com.empresa.ecommerce_backend.enums.AuthProvider;
import com.empresa.ecommerce_backend.model.*;
import com.empresa.ecommerce_backend.repository.*;
import com.empresa.ecommerce_backend.service.interfaces.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.stream.Collectors;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtServiceImpl jwtServiceImpl;
    private final MailServiceImpl mailServiceImpl;
    private final LoginAttemptServiceImpl loginAttemptServiceImpl;

    @Transactional
    public ServiceResult<RegisterUserResponse> registerUser(RegisterUserRequest dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            return new ServiceResult<>(false, "El email ya está registrado.", null);
        }

        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setVerified(false);
        user.setAuthProvider(AuthProvider.LOCAL);

        Role defaultRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new RuntimeException("Rol CUSTOMER no encontrado"));
        user.getRoles().add(defaultRole);

        User saved = userRepository.save(user);

        // ✅ Generar y enviar token de verificación
        String token = jwtServiceImpl.generateEmailVerificationToken(saved.getEmail());
        mailServiceImpl.sendVerificationEmail(saved.getEmail(), token);

        RegisterUserResponse response = new RegisterUserResponse(saved.getId(), saved.getEmail());
        return new ServiceResult<>(true, null, response);
    }

    public ServiceResult<Void> verifyEmail(String token) {
        String email = jwtServiceImpl.validateEmailVerificationToken(token);
        if (email == null) {
            return new ServiceResult<>(false, "Token inválido o expirado.", null);
        }

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return new ServiceResult<>(false, "Usuario no encontrado.", null);
        }

        User user = optionalUser.get();
        if (user.isVerified()) {
            return new ServiceResult<>(true, "La cuenta ya estaba verificada.", null);
        }

        user.setVerified(true);
        userRepository.save(user);
        return new ServiceResult<>(true, "Cuenta verificada exitosamente.", null);
    }

    public ServiceResult<LoginResponse> login(LoginRequest request, String ip) {
        try {
            Optional<User> optionalUser = userRepository.findByEmail(request.email());

            if (optionalUser.isEmpty()) {
                loginAttemptServiceImpl.logAttempt(null, ip, false, "Usuario no encontrado.");
                return new ServiceResult<>(false, "Usuario no encontrado.", null);
            }

            User user = optionalUser.get();

            if (user.getPassword() == null || user.getPassword().isBlank()) {
                loginAttemptServiceImpl.logAttempt(user, ip, false, "Sin contraseña (OAuth).");
                return new ServiceResult<>(false,
                        "Este usuario no tiene contraseña configurada. Inicie sesión con Google o Microsoft.", null);
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );

            String token = jwtServiceImpl.generateToken(authentication);

            loginAttemptServiceImpl.logAttempt(user, ip, true, null);

            return new ServiceResult<>(true, null, new LoginResponse(token));

        } catch (BadCredentialsException e) {
            userRepository.findByEmail(request.email())
                    .ifPresentOrElse(
                            user -> loginAttemptServiceImpl.logAttempt(user, ip, false, "Credenciales inválidas."),
                            () -> loginAttemptServiceImpl.logAttempt(null, ip, false, "Credenciales inválidas.")
                    );
            return new ServiceResult<>(false, "Credenciales inválidas.", null);

        } catch (Exception e) {
            loginAttemptServiceImpl.logAttempt(null, ip, false, "Error de autenticación.");
            return new ServiceResult<>(false, "Error de autenticación.", null);
        }
    }

    public ServiceResult<String> handleOAuthCallback(OAuthCallbackRequest dto, String ip) {
        try {
            boolean tokenValido = jwtServiceImpl.verifyIdToken(dto.getIdToken(), dto.getProvider());
            if (!tokenValido) {
                loginAttemptServiceImpl.logAttempt(null, ip, false, "ID token inválido");
                return new ServiceResult<>(false, "ID token inválido", null);
            }

            User user = userRepository.findByEmail(dto.getEmail()).orElseGet(() -> {
                Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                        .orElseThrow(() -> new RuntimeException("Rol CUSTOMER no encontrado"));

                String firstName = (dto.getFirstName() != null && !dto.getFirstName().isBlank())
                        ? dto.getFirstName()
                        : "Usuario";
                String lastName = (dto.getLastName() != null && !dto.getLastName().isBlank())
                        ? dto.getLastName()
                        : "OAuth";

                AuthProvider provider = switch (dto.getProvider().toLowerCase()) {
                    case "google" -> AuthProvider.GOOGLE;
                    case "azure-ad" -> AuthProvider.AZURE_AD;
                    default -> AuthProvider.LOCAL;
                };

                User nuevo = new User();
                nuevo.setEmail(dto.getEmail());
                nuevo.setFirstName(firstName);
                nuevo.setLastName(lastName);
                nuevo.setVerified(true);
                nuevo.setPassword(null);
                nuevo.setAuthProvider(provider);
                nuevo.setRoles(Set.of(customerRole));

                return userRepository.save(nuevo);
            });

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

            String jwt = jwtServiceImpl.generateToken(auth);

            loginAttemptServiceImpl.logAttempt(user, ip, true, null); // ✅ intento exitoso
            return new ServiceResult<>(true, null, jwt);

        } catch (Exception e) {
            loginAttemptServiceImpl.logAttempt(null, ip, false, "Error al verificar ID token");
            return new ServiceResult<>(false, "Error al verificar ID token", null);
        }
    }





}
