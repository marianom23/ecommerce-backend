package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.model.Order;
import com.empresa.ecommerce_backend.model.Payment;

public interface EmailService {
    void sendOrderConfirmation(Order order);
    void sendTransferPendingAdminNotification(Order order, Payment payment);
    void sendPaymentApprovedNotification(Order order);
}
