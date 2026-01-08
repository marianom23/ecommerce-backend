package com.empresa.ecommerce_backend.service;

import com.facebook.ads.sdk.APIContext;
import com.facebook.ads.sdk.serverside.*;
import com.empresa.ecommerce_backend.model.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;

@Slf4j
@Service
public class MetaPixelService {

    @Value("${META_PIXEL_ID}")
    private String pixelId;

    @Value("${META_ACCESS_TOKEN}")
    private String accessToken;

    @Value("${meta.test-mode:false}")
    private boolean testMode;

    @Value("${META_TEST_CODE:}")
    private String testCode;

    /**
     * Envía un evento a Meta Conversions API de forma asíncrona.
     *
     * @param eventName Nombre del evento (Purchase, AddToCart, etc.)
     * @param request   HttpServletRequest para extraer IP, User-Agent, cookies
     * @param user      Usuario autenticado (opcional)
     * @param value     Valor monetario del evento (opcional)
     * @param currency  Moneda (ej: "ARS")
     * @param eventId   ID único para deduplicación con frontend (opcional)
     */
    @Async
    public void sendEvent(String eventName, HttpServletRequest request, User user, 
                          Double value, String currency, String eventId) {
        try {
            APIContext context = new APIContext(accessToken);
            if (testMode) context.enableDebug(true);

            // 1. Datos del Usuario
            UserData userData = new UserData()
                    .clientIpAddress(extractClientIp(request))
                    .clientUserAgent(request.getHeader("User-Agent"));

            // Email hasheado (si hay usuario)
            if (user != null && user.getEmail() != null) {
                String hashedEmail = hashSHA256(user.getEmail().toLowerCase().trim());
                userData.emails(Arrays.asList(hashedEmail));
            }

            // Cookies de Facebook (_fbp, _fbc)
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("_fbp".equals(cookie.getName())) {
                        userData.fbp(cookie.getValue());
                    }
                    if ("_fbc".equals(cookie.getName())) {
                        userData.fbc(cookie.getValue());
                    }
                }
            }

            // 2. Crear el Evento
            Event event = new Event();
            event.eventName(eventName);
            event.eventTime(System.currentTimeMillis() / 1000L); // Unix timestamp
            event.userData(userData);
            event.eventSourceUrl(request.getRequestURL().toString());
            event.actionSource(ActionSource.website);

            // Event ID para deduplicación
            if (eventId != null && !eventId.isBlank()) {
                event.eventId(eventId);
            }

            // Custom Data (valor monetario)
            if (value != null && value > 0) {
                CustomData customData = new CustomData()
                        .value(value.floatValue())
                        .currency(currency != null ? currency : "ARS");
                event.customData(customData);
            }

            // 3. Enviar a Meta
            EventRequest eventRequest = new EventRequest(pixelId, context);
            eventRequest.addDataItem(event);

            if (testMode && testCode != null && !testCode.isEmpty()) {
                eventRequest.testEventCode(testCode); // Para ver en Test Events
            }

            EventResponse response = eventRequest.execute();
            log.info("✅ Meta Event Sent: {} | Events Received: {}", 
                     eventName, response.getEventsReceived());

        } catch (Exception e) {
            log.error("❌ Error sending Meta event: {}", eventName, e);
        }
    }

    /**
     * Versión simplificada sin eventId
     */
    @Async
    public void sendEvent(String eventName, HttpServletRequest request, User user, Double value) {
        sendEvent(eventName, request, user, value, "ARS", null);
    }

    /**
     * Hash SHA-256 para PII (emails, teléfonos)
     */
    private String hashSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Error hashing input", e);
            return null;
        }
    }

    /**
     * Extrae IP del request (considera X-Forwarded-For para proxies/load balancers)
     */
    private String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Si hay múltiples IPs (proxy chain), tomar la primera
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
