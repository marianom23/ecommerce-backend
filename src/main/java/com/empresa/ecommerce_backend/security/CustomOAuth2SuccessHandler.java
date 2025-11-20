// src/main/java/com/empresa/ecommerce_backend/security/CustomOAuth2SuccessHandler.java
package com.empresa.ecommerce_backend.security;

import com.empresa.ecommerce_backend.dto.response.LoginResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.service.OAuth2UserProcessor;
import com.empresa.ecommerce_backend.web.AuthCookieManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2UserProcessor oAuth2UserProcessor;
    private final AuthCookieManager authCookieManager;

    @Value("${FRONT_BASE_URL:http://localhost:3000}")
    private String frontendBaseUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        System.out.println("🔥 CustomOAuth2SuccessHandler EJECUTADO");

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String ip = extractClientIp(request);

        ServiceResult<LoginResponse> result =
                oAuth2UserProcessor.processOAuth2User(oauthUser, registrationId, ip);

        if (result.getStatus() != HttpStatus.OK || result.getData() == null) {
            String msg = result.getMessage() != null ? result.getMessage() : "Error en login OAuth2";
            String errorUrl = frontendBaseUrl + "/login?oauth_error=" +
                    URLEncoder.encode(msg, StandardCharsets.UTF_8);
            response.sendRedirect(errorUrl);
            return;
        }

        LoginResponse login = result.getData();

        // 🔐 Setear cookie httpOnly con el JWT
        authCookieManager.setAuthCookie(response, login.getToken(), request.isSecure());

        // `next` si lo querés respetar (si no, redirigí siempre a "/")
        String next = Optional.ofNullable(request.getParameter("next")).orElse("/");

        String redirectUrl = frontendBaseUrl + next;
        System.out.println("👉 Redirecting to: " + redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        return (xfHeader != null && !xfHeader.isBlank())
                ? xfHeader.split(",")[0]
                : request.getRemoteAddr();
    }
}
