package com.empresa.ecommerce_backend.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración para cargar variables desde archivo .env
 */
public class DotEnvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            // Cargar .env desde la raíz del proyecto
            Dotenv dotenv = Dotenv.configure()
                    .directory(".")  // Directorio raíz
                    .ignoreIfMissing() // No fallar si no existe
                    .load();

            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            Map<String, Object> envProperties = new HashMap<>();

            // Convertir las variables de .env a propiedades de Spring
            dotenv.entries().forEach(entry -> {
                envProperties.put(entry.getKey(), entry.getValue());
                // También las ponemos como system properties
                System.setProperty(entry.getKey(), entry.getValue());
            });

            if (!envProperties.isEmpty()) {
                MapPropertySource propertySource = new MapPropertySource("dotenv", envProperties);
                environment.getPropertySources().addFirst(propertySource);
                System.out.println("✅ Cargadas " + envProperties.size() + " variables desde .env");
            } else {
                System.out.println("⚠️  No se encontraron variables en .env o archivo no existe");
            }

        } catch (Exception e) {
            System.err.println("❌ Error cargando .env: " + e.getMessage());
        }
    }
}