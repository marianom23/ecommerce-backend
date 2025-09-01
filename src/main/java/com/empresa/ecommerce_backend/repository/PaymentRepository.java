// src/main/java/com/empresa/ecommerce_backend/repository/PaymentRepository.java
package com.empresa.ecommerce_backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.empresa.ecommerce_backend.enums.PaymentMethod;
import com.empresa.ecommerce_backend.enums.PaymentStatus;
import com.empresa.ecommerce_backend.model.Payment;

public interface PaymentRepository extends BaseRepository<Payment, Long> {
    Optional<Payment> findByProviderAndProviderPaymentId(String provider, String providerPaymentId);
    List<Payment> findAllByStatusInAndExpiresAtBefore(List<PaymentStatus> statuses, LocalDateTime before);
}
