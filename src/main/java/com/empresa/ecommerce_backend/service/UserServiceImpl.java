package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.*;
import com.empresa.ecommerce_backend.dto.response.*;
import com.empresa.ecommerce_backend.enums.RoleName;
import com.empresa.ecommerce_backend.enums.AuthProvider;
import com.empresa.ecommerce_backend.mapper.UserMapper;
import com.empresa.ecommerce_backend.model.*;
import com.empresa.ecommerce_backend.repository.*;
import com.empresa.ecommerce_backend.service.interfaces.JwtService;
import com.empresa.ecommerce_backend.service.interfaces.LoginAttemptService;
import com.empresa.ecommerce_backend.service.interfaces.MailService;
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
    private final JwtService jwtService;
    private final MailService mailService;
    private final UserMapper userMapper;
    private final LoginAttemptService loginAttemptService;

    @Transactional
    public ServiceResult<RegisterUserResponse> registerUser(RegisterUserRequest dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            return new ServiceResult<>(false, "El email ya está registrado.", null);
        }

        User user = userMapper.toEntity(dto);
        user.setVerified(false);
        user.setAuthProvider(AuthProvider.LOCAL);

        Role defaultRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new RuntimeException("Rol CUSTOMER no encontrado"));
        user.getRoles().add(defaultRole);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        User saved = userRepository.save(user);

        // ✅ Generar y enviar token de verificación
        String token = jwtService.generateEmailVerificationToken(saved.getEmail());
        mailService.sendVerificationEmail(saved.getEmail(), token);

        RegisterUserResponse response = userMapper.toRegisterResponse(saved);
        return new ServiceResult<>(true, null, response);
    }

    public ServiceResult<Void> verifyEmail(String token) {
        String email = jwtService.validateEmailVerificationToken(token);
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
                loginAttemptService.logAttempt(null, ip, false, "Usuario no encontrado.");
                return new ServiceResult<>(false, "Usuario no encontrado.", null);
            }

            User user = optionalUser.get();

            if (user.getPassword() == null || user.getPassword().isBlank()) {
                loginAttemptService.logAttempt(user, ip, false, "Sin contraseña (OAuth).");
                return new ServiceResult<>(false,
                        "Este usuario no tiene contraseña configurada. Inicie sesión con Google o Microsoft.", null);
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
            return new ServiceResult<>(true, null, loginResponse);


        } catch (BadCredentialsException e) {
            userRepository.findByEmail(request.email())
                    .ifPresentOrElse(
                            user -> loginAttemptService.logAttempt(user, ip, false, "Credenciales inválidas."),
                            () -> loginAttemptService.logAttempt(null, ip, false, "Credenciales inválidas.")
                    );
            return new ServiceResult<>(false, "Credenciales inválidas.", null);

        } catch (Exception e) {
            loginAttemptService.logAttempt(null, ip, false, "Error de autenticación.");
            return new ServiceResult<>(false, "Error de autenticación.", null);
        }
    }

    public ServiceResult<String> handleOAuthCallback(OAuthCallbackRequest dto, String ip) {
        try {
            boolean tokenValido = jwtService.verifyIdToken(dto.getIdToken(), dto.getProvider());
            if (!tokenValido) {
                loginAttemptService.logAttempt(null, ip, false, "ID token inválido");
                return new ServiceResult<>(false, "ID token inválido", null);
            }

            User user = userRepository.findByEmail(dto.getEmail()).orElseGet(() -> {
                Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                        .orElseThrow(() -> new RuntimeException("Rol CUSTOMER no encontrado"));

                User nuevo = userMapper.fromOAuthDto(dto);
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

            String jwt = jwtService.generateToken(auth);

            loginAttemptService.logAttempt(user, ip, true, null); // ✅ intento exitoso
            return new ServiceResult<>(true, null, jwt);

        } catch (Exception e) {
            loginAttemptService.logAttempt(null, ip, false, "Error al verificar ID token");
            return new ServiceResult<>(false, "Error al verificar ID token", null);
        }
    }





}
