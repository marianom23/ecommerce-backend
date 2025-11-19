package com.empresa.ecommerce_backend.config;

import com.empresa.ecommerce_backend.security.JwtAuthenticationFilter;
// import com.empresa.ecommerce_backend.security.CustomOAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Público real
                        .requestMatchers(
                                "/api/login",
                                "/api/register",
                                "/api/verify-email",
                                "/swagger-ui/**",
                                "/api-docs/**",
                                "/oauth2/**",
                                "/login/**",
                                "/api/oauth2/callback"
                        ).permitAll()

                        // ✅ Webhook de Mercado Pago (sin auth)
                        .requestMatchers(HttpMethod.POST, "/api/webhooks/mercadopago").permitAll()

                        // ⛔️ SOLO attach requiere auth (todos los métodos sobre esa ruta)
                        .requestMatchers("/api/cart/attach").authenticated()

                        // ✅ TODO lo demás de carrito es público (GET/POST/PUT/DELETE, etc.)
                        .requestMatchers("/api/cart/**").permitAll()

                        // Productos (lectura pública)
                        // PÚBLICOS: /p/products y facetas
                        .requestMatchers(HttpMethod.GET, "/p/products/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/p/products/**").permitAll()

                        // ⚠️ Si tu front pega a /api/p/products/** (Next JS proxy con prefijo /api), permití también estas:
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/api/products/**").permitAll()

                        // Productos/variants solo ADMIN
                        .requestMatchers(HttpMethod.POST,   "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/api/variants/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/variants/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/variants/**").hasRole("ADMIN")

                        // Zonas admin/manager ya existentes
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/manager/**").hasAnyRole("ADMIN", "MANAGER")

                        // Preflight CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Resto autenticado
                        .anyRequest().authenticated()
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
                "https://ecommerce-frontend-ebon-omega.vercel.app"));
        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")); // <-- PATCH
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
