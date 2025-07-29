package com.empresa.ecommerce_backend.service.interfaces;

public interface MailService {
    void sendVerificationEmail(String to, String token);
}