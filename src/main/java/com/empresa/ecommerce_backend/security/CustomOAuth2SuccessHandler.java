package com.empresa.ecommerce_backend.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import com.empresa.ecommerce_backend.service.JwtService;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        // 🛡️ Generar JWT para el usuario autenticado por Google/Microsoft
        String jwt = jwtService.generateToken(authentication);

        // 🌐 Redirigir al frontend con el token en la URL
        String redirectUrl = "http://localhost:4200/oauth2/success?token=" + jwt;

        response.sendRedirect(redirectUrl);
    }
}
