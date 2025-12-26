package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.exception.EmailSendingException;
import com.empresa.ecommerce_backend.model.Order;
import com.empresa.ecommerce_backend.model.Payment;
import com.empresa.ecommerce_backend.service.interfaces.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${resend.api-key}")
    private String resendApiKey;

    @Value("${app.mail.from:onboarding@resend.dev}")
    private String fromEmail;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.backend.base-url}")
    private String backendBaseUrl;

    @Value("${app.frontend.base-url:}")
    private String frontendBaseUrl;

    @Value("${app.mail.subject.verify:Verifica tu cuenta}")
    private String verifySubject;

    @Async("mailExecutor")
    @Override
    public void sendOrderConfirmation(Order order) {
        String subject = "Confirmación de Orden #" + order.getOrderNumber();
        String body = buildOrderHtml(order, "¡Gracias por tu compra!", "Tu orden ha sido recibida y está siendo procesada.");
        
        // Email al cliente
        sendHtmlEmail(order.getUser().getEmail(), subject, body);
        
        // Copia al admin
        sendHtmlEmail(adminEmail, subject, body);
    }

    @Async("mailExecutor")
    @Override
    public void sendTransferPendingAdminNotification(Order order, Payment payment) {
        String subject = "[ADMIN] Nueva Transferencia Informada - Orden #" + order.getOrderNumber();
        String body = "<h1>Nueva Transferencia Informada</h1>" +
                "<p>El usuario ha informado una transferencia para la orden <b>" + order.getOrderNumber() + "</b>.</p>" +
                "<ul>" +
                "<li>Monto: $" + payment.getAmount() + "</li>" +
                "<li>Referencia: " + payment.getTransferReference() + "</li>" +
                "<li>Comprobante: <a href='" + (payment.getReceiptUrl() != null ? payment.getReceiptUrl() : "#") + "'>Ver Comprobante</a></li>" +
                "</ul>" +
                "<p>Por favor, verificá la acreditación y aprobá el pago en el panel de administración.</p>";
        
        sendHtmlEmail(adminEmail, subject, body);
    }

    @Async("mailExecutor")
    @Override
    public void sendPaymentApprovedNotification(Order order) {
        String subject = "Pago Aprobado - Orden #" + order.getOrderNumber();
        String body = buildOrderHtml(order, "¡Pago Aprobado!", "Hemos recibido tu pago correctamente. Pronto prepararemos tu pedido.");
        sendHtmlEmail(order.getUser().getEmail(), subject, body);
    }

    @Async("mailExecutor")
    @Override
    public void sendVerificationEmail(String to, String token) {
        if (to == null || to.isBlank()) throw new EmailSendingException("Destinatario vacío", null);
        if (token == null || token.isBlank()) throw new EmailSendingException("Token vacío", null);

        final String base = normalizeBase(!isBlank(frontendBaseUrl) ? frontendBaseUrl : backendBaseUrl);
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

        sendHtmlEmail(to, verifySubject, htmlContent);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            Map<String, Object> payload = Map.of(
                    "from", fromEmail,
                    "to", to,
                    "subject", subject,
                    "html", htmlBody
            );

            String jsonBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                log.info("Email enviado exitosamente a {} (Resend ID: {})", to, response.body());
            } else {
                log.error("Error enviando email a {}: Status {} - Body {}", to, response.statusCode(), response.body());
            }

        } catch (Exception e) {
            log.error("Excepción enviando email a {}", to, e);
        }
    }

    private String buildOrderHtml(Order order, String title, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h2>").append(title).append("</h2>");
        sb.append("<p>").append(message).append("</p>");
        sb.append("<h3>Detalle de la Orden #").append(order.getOrderNumber()).append("</h3>");
        sb.append("<table border='1' cellpadding='5' cellspacing='0'>");
        sb.append("<tr><th>Producto</th><th>Cant</th><th>Total</th></tr>");
        
        if (order.getItems() != null) {
            order.getItems().forEach(item -> {
                sb.append("<tr>");
                sb.append("<td>").append(item.getProductName()).append("</td>");
                sb.append("<td>").append(item.getQuantity()).append("</td>");
                sb.append("<td>$").append(item.getLineTotal()).append("</td>");
                sb.append("</tr>");
            });
        }
        
        sb.append("</table>");
        sb.append("<p><b>Total: $").append(order.getTotalAmount()).append("</b></p>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String normalizeBase(String base) {
        return base != null ? base.replaceAll("/+$", "") : "";
    }
}
