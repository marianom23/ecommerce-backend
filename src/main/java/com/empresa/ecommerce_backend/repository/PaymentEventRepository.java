// src/main/java/com/empresa/ecommerce_backend/repository/PaymentEventRepository.java
package com.empresa.ecommerce_backend.repository;

import java.util.List;

import com.empresa.ecommerce_backend.model.PaymentEvent;

public interface PaymentEventRepository extends BaseRepository<PaymentEvent, Long> {

    List<PaymentEvent> findByPayment_IdOrderByEventAtAsc(Long paymentId);
}
