package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.model.Order;
import com.empresa.ecommerce_backend.model.Payment;
import com.empresa.ecommerce_backend.service.interfaces.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Async
    @Override
    public void sendOrderConfirmation(Order order) {
        // Email al cliente
        String subject = "Confirmación de Orden #" + order.getOrderNumber();
        String body = buildOrderHtml(order, "¡Gracias por tu compra!", "Tu orden ha sido recibida y está siendo procesada.");
        sendHtmlEmail(order.getUser().getEmail(), subject, body);
        
        // Copia al admin
        sendHtmlEmail(adminEmail, subject, body);
    }

    @Async
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

    @Async
    @Override
    public void sendPaymentApprovedNotification(Order order) {
        String subject = "Pago Aprobado - Orden #" + order.getOrderNumber();
        String body = buildOrderHtml(order, "¡Pago Aprobado!", "Hemos recibido tu pago correctamente. Pronto prepararemos tu pedido.");
        sendHtmlEmail(order.getUser().getEmail(), subject, body);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            
            mailSender.send(message);
            log.info("Email enviado a {} con asunto '{}'", to, subject);
        } catch (MessagingException e) {
            log.error("Error enviando email a {}", to, e);
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
        
        order.getItems().forEach(item -> {
            sb.append("<tr>");
            sb.append("<td>").append(item.getProductName()).append("</td>");
            sb.append("<td>").append(item.getQuantity()).append("</td>");
            sb.append("<td>$").append(item.getLineTotal()).append("</td>");
            sb.append("</tr>");
        });
        
        sb.append("</table>");
        sb.append("<p><b>Total: $").append(order.getTotalAmount()).append("</b></p>");
        sb.append("</body></html>");
        return sb.toString();
    }
}
