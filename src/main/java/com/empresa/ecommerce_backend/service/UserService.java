package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.*;
import com.empresa.ecommerce_backend.dto.response.*;
import com.empresa.ecommerce_backend.enums.RoleName;
import com.empresa.ecommerce_backend.model.*;
import com.empresa.ecommerce_backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import java.util.stream.Collectors;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MailService mailService;

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

        Role defaultRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new RuntimeException("Rol CUSTOMER no encontrado"));
        user.getRoles().add(defaultRole);

        User saved = userRepository.save(user);

        // ✅ Generar y enviar token de verificación
        String token = jwtService.generateEmailVerificationToken(saved.getEmail());
        mailService.sendVerificationEmail(saved.getEmail(), token);

        RegisterUserResponse response = new RegisterUserResponse(saved.getId(), saved.getEmail());
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

    public ServiceResult<LoginResponse> login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );

            String token = jwtService.generateToken(authentication);
            return new ServiceResult<>(true, null, new LoginResponse(token));

        } catch (BadCredentialsException e) {
            return new ServiceResult<>(false, "Credenciales inválidas.", null);
        } catch (Exception e) {
            return new ServiceResult<>(false, "Error de autenticación.", null);
        }
    }

    public ServiceResult<String> handleOAuthCallback(OAuthCallbackRequest dto) {
        try {
            System.out.println("👉 Provider: " + dto.getProvider());
            System.out.println("👉 ID Token: " + (dto.getIdToken() != null ? "Recibido" : "NULO"));

            // Verificamos el ID Token con Nimbus
            boolean tokenValido = jwtService.verifyIdToken(dto.getIdToken(), dto.getProvider());
            if (!tokenValido) {
                return new ServiceResult<>(false, "ID token inválido", null);
            }

            // Buscar o registrar al usuario
            User user = userRepository.findByEmail(dto.getEmail()).orElseGet(() -> {
                Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                        .orElseThrow(() -> new RuntimeException("Rol CUSTOMER no encontrado"));

                String firstName = (dto.getFirstName() != null && !dto.getFirstName().isBlank())
                        ? dto.getFirstName()
                        : "Usuario";
                String lastName = (dto.getLastName() != null && !dto.getLastName().isBlank())
                        ? dto.getLastName()
                        : "OAuth";

                User nuevo = new User();
                nuevo.setEmail(dto.getEmail());
                nuevo.setFirstName(firstName);
                nuevo.setLastName(lastName);
                nuevo.setVerified(true);
                nuevo.setPassword(UUID.randomUUID().toString());
                nuevo.setRoles(Set.of(customerRole));

                return userRepository.save(nuevo);
            });

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    user.getEmail(),
                    null,
                    user.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                            .collect(Collectors.toList())
            );

            String jwt = jwtService.generateToken(auth);
            return new ServiceResult<>(true, null, jwt);

        } catch (Exception e) {
            return new ServiceResult<>(false, "Error al verificar ID token", null);
        }
    }



}
