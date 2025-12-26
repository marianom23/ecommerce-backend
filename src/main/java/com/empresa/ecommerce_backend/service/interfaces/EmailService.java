package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.model.Order;
import com.empresa.ecommerce_backend.model.Payment;

public interface EmailService {
    void sendOrderConfirmation(Long orderId);
    void sendTransferPendingAdminNotification(Long orderId, Long paymentId);
    void sendPaymentApprovedNotification(Long orderId);
    void sendVerificationEmail(String to, String token);
}
