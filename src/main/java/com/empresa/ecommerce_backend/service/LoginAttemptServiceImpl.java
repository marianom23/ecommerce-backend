package com.empresa.ecommerce_backend.service;
import com.empresa.ecommerce_backend.service.interfaces.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.empresa.ecommerce_backend.repository.*;
import com.empresa.ecommerce_backend.model.LoginAttempt;
import com.empresa.ecommerce_backend.model.User;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private final LoginAttemptRepository loginAttemptRepository;

    public void logAttempt(User user, String ipAddress, boolean success, String reason) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setUser(user);
        attempt.setIpAddress(ipAddress);
        attempt.setSuccess(success);
        attempt.setReason(reason);
        attempt.setAttemptAt(LocalDateTime.now());

        loginAttemptRepository.save(attempt);
    }
}
