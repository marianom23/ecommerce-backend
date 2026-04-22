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
import java.util.List;

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
    /**
     * Envía un evento a Meta Conversions API de forma asíncrona con datos enriquecidos.
     */
    @Async
    public void sendEvent(String eventName, 
                          String clientIp, 
                          String userAgent, 
                          String sourceUrl,
                          List<String> fbpFbcCookies,
                          String email,
                          String firstName,
                          String lastName,
                          Long userId,        // <--- NUEVO: Para external_id
                          List<Content> contents, // <--- NUEVO: Para Catálogo
                          Double value, 
                          String currency, 
                          String eventId) {
        try {
            APIContext context = new APIContext(accessToken);
            if (testMode) context.enableDebug(true);

            // 1. Datos del Usuario (PII)
            UserData userData = new UserData()
                    .clientIpAddress(clientIp)
                    .clientUserAgent(userAgent);

            if (email != null && !email.isBlank()) {
                userData.emails(Arrays.asList(hashSHA256(email.toLowerCase().trim())));
            }

            if (firstName != null && !firstName.isBlank()) {
                userData.firstNames(Arrays.asList(hashSHA256(firstName.toLowerCase().trim())));
            }
            if (lastName != null && !lastName.isBlank()) {
                userData.lastNames(Arrays.asList(hashSHA256(lastName.toLowerCase().trim())));
            }

            // External ID es el ID de tu DB (muy fuerte para matching)
            if (userId != null) {
                userData.externalIds(Arrays.asList(hashSHA256(userId.toString())));
            }

            if (fbpFbcCookies != null && fbpFbcCookies.size() >= 1 && fbpFbcCookies.get(0) != null) {
                userData.fbp(fbpFbcCookies.get(0));
            }
            if (fbpFbcCookies != null && fbpFbcCookies.size() >= 2 && fbpFbcCookies.get(1) != null) {
                userData.fbc(fbpFbcCookies.get(1));
            }

            // 2. Crear el Evento
            Event event = new Event();
            event.eventName(eventName);
            event.eventTime(System.currentTimeMillis() / 1000L);
            event.userData(userData);
            event.eventSourceUrl(sourceUrl);
            event.actionSource(ActionSource.website);

            if (eventId != null && !eventId.isBlank()) {
                event.eventId(eventId);
            }

            // 3. Custom Data (Valor + Productos)
            CustomData customData = new CustomData();
            customData.setCurrency(currency != null ? currency : "ARS");

            if (value != null && value > 0) {
                customData.setValue(value.floatValue());
            }

            if (contents != null && !contents.isEmpty()) {
                customData.setContents(contents);
                customData.setContentType("product");
            }
            
            event.customData(customData);

            // 4. Enviar a Meta
            EventRequest eventRequest = new EventRequest(pixelId, context);
            eventRequest.addDataItem(event);

            if (testMode && testCode != null && !testCode.isEmpty()) {
                eventRequest.testEventCode(testCode);
            }

            EventResponse response = eventRequest.execute();
            log.info("✅ Meta Event Sent: {} | ID: {} | Received: {}", 
                     eventName, eventId, response.getEventsReceived());

        } catch (Exception e) {
            log.error("❌ Error sending Meta event: {}", eventName, e);
        }
    }

    /**
     * Helper para extraer IP del request de forma segura (síncrona).
     */
    public String extractClientIp(HttpServletRequest request) {
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
    
    public List<String> extractFbpFbc(HttpServletRequest request) {
        String fbp = null;
        String fbc = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("_fbp".equals(cookie.getName())) fbp = cookie.getValue();
                if ("_fbc".equals(cookie.getName())) fbc = cookie.getValue();
            }
        }
        return Arrays.asList(fbp, fbc);
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

}
