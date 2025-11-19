package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.service.interfaces.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCleanupScheduler {

    private final PaymentService paymentService;


    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void cleanupExpiredPayments() {

        int expired = paymentService.expireOverduePayments();

        if (expired > 0) {
            log.info("💳 Expiraron {} pagos y se cancelaron sus órdenes.", expired);
        } else {
            // log.debug para que no ensucie logs
            log.debug("⏳ No hay pagos vencidos todavía.");
        }
    }
}
