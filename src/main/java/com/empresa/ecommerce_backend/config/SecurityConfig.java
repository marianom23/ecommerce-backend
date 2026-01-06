package com.empresa.ecommerce_backend.config;

import com.empresa.ecommerce_backend.security.JwtAuthenticationFilter;
import com.empresa.ecommerce_backend.security.CustomOAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                // 🔥 Nada de guardar seguridad en sesión
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 🔥 Cuando no hay auth, devolvé 401 JSON (no redirección a /login)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\":\"UNAUTHORIZED\"}");
                        })
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/login",
                                "/api/register",
                                "/api/verify-email",
                                "/swagger-ui/**",
                                "/api-docs/**",
                                "/api/oauth2/callback"
                        ).permitAll()

                        // ya NO necesitamos /login/**, lo podés quitar
                        .requestMatchers("/oauth2/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/webhooks/mercadopago").permitAll()

                        .requestMatchers("/api/cart/attach").authenticated()
                        .requestMatchers("/api/cart/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/banners/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/api/banners/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/api/products/**").permitAll()

                        .requestMatchers(HttpMethod.POST,   "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/api/variants/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/variants/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/variants/**").hasRole("ADMIN")

                        // Reviews: GET público para ver reviews, POST/PUT/DELETE requieren auth
                        .requestMatchers(HttpMethod.GET, "/api/reviews/product/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/user/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/best").permitAll() // 👈 NUEVO
                        .requestMatchers(HttpMethod.OPTIONS, "/api/reviews/**").permitAll()

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/manager/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/suppliers/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .anyRequest().authenticated()
                )

                // 🔥 Desactivamos login de formulario y basic auth
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // Dejamos solo OAuth2 para el flujo de Google
                .oauth2Login(oauth -> oauth
                        .successHandler(oAuth2SuccessHandler)
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "https://ecommerce-frontend-ebon-omega.vercel.app",
                "https://hornerotech.com.ar",
                "https://www.hornerotech.com.ar",
                "https://ecommerce-backoffice-alpha.vercel.app"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
