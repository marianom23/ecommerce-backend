package com.empresa.ecommerce_backend.security;

import com.empresa.ecommerce_backend.model.User;
import com.empresa.ecommerce_backend.repository.UserRepository;
import com.empresa.ecommerce_backend.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        String firstName = (String) attributes.getOrDefault("given_name", "Usuario");
        String lastName = (String) attributes.getOrDefault("family_name", "");

        // 🔍 Buscar usuario por email o crear uno nuevo
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User nuevo = new User();
            nuevo.setEmail(email);
            nuevo.setFirstName(firstName);
            nuevo.setLastName(lastName);
            nuevo.setVerified(true);
            nuevo.setPassword(""); // No se necesita para OAuth2
            return userRepository.save(nuevo);
        });

        // 🔐 Generar JWT
        String jwt = jwtService.generateToken(authentication);

        // 🔁 Redirigir al frontend con el token
        String redirectUrl = "http://localhost:4200/oauth2/success?token=" + jwt;
        response.sendRedirect(redirectUrl);
    }
}
