// src/main/java/com/empresa/ecommerce_backend/events/UserRegisteredListener.java
package com.empresa.ecommerce_backend.events;

import com.empresa.ecommerce_backend.service.interfaces.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserRegisteredListener {

    private final MailService mailService;

    @Async("mailExecutor") // o @Async simple si no definiste un executor con nombre
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        mailService.sendVerificationEmail(event.email(), event.token());
    }
}
