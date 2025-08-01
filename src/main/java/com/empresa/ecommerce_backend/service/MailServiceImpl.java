// src/main/java/com/empresa/ecommerce_backend/service/MailServiceImpl.java
package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.exception.EmailSendingException;
import com.empresa.ecommerce_backend.service.interfaces.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Value("${app.backend.base-url}")   // p.ej. http://localhost:8080 (sin / final)
    private String backendBaseUrl;

    // Opcional: si querés que el link apunte al front
    @Value("${app.frontend.base-url:}") // p.ej. http://localhost:3000 (sin / final)
    private String frontendBaseUrl;

    @Value("${app.mail.from:noreply@empresa.com}")
    private String fromAddress;

    @Value("${app.mail.subject.verify:Verifica tu cuenta}")
    private String verifySubject;

    @Async("mailExecutor") // si NO tenés un executor, usa @Async sin nombre
    @Override
    public void sendVerificationEmail(String to, String token) {
        if (to == null || to.isBlank()) {
            throw new EmailSendingException("Destinatario vacío", null);
        }
        if (token == null || token.isBlank()) {
            throw new EmailSendingException("Token de verificación vacío", null);
        }

        // Preferí FRONT si está configurado; si no, apuntá al BACK
        final String base = normalizeBase(!isBlank(frontendBaseUrl) ? frontendBaseUrl : backendBaseUrl);
        // Si usás front: /verify-email?token=... | Si usás back: /api/verify-email?token=...
        final String path = !isBlank(frontendBaseUrl) ? "/verify-email" : "/api/verify-email";

        String verificationUrl = UriComponentsBuilder
                .fromHttpUrl(base)
                .path(path)
                .queryParam("token", token)
                .build(true)
                .toUriString();

        String htmlContent = """
            <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2>¡Bienvenido!</h2>
                    <p>Haz clic en el botón para verificar tu cuenta:</p>
                    <p>
                      <a href="%s" style="display:inline-block;padding:10px 20px;background-color:#4CAF50;color:white;text-decoration:none;border-radius:5px;">
                        Verificar cuenta
                      </a>
                    </p>
                    <p>O copia y pega este enlace en tu navegador:</p>
                    <p><a href="%s">%s</a></p>
                </body>
            </html>
        """.formatted(verificationUrl, verificationUrl, verificationUrl);

        String textContent = """
            ¡Bienvenido!
            
            Verificá tu cuenta usando este enlace:
            %s
        """.formatted(verificationUrl);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            // true => multipart; "UTF-8" para caracteres
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(verifySubject);
            helper.setText(textContent, htmlContent); // (texto plano, HTML)

            mailSender.send(message);

        } catch (MessagingException | MailException e) {
            throw new EmailSendingException("Error al enviar el correo de verificación", e);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String normalizeBase(String base) {
        return base != null ? base.replaceAll("/+$", "") : "";
    }
}
