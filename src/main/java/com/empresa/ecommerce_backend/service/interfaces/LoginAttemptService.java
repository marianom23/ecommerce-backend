package com.empresa.ecommerce_backend.service.interfaces;
import com.empresa.ecommerce_backend.model.User;

public interface LoginAttemptService {
    void logAttempt(User user, String ipAddress, boolean success, String reason);
}