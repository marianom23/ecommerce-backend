// src/main/java/com/empresa/ecommerce_backend/repository/ChargebackRepository.java
package com.empresa.ecommerce_backend.repository;

import java.util.List;
import java.util.Optional;

import com.empresa.ecommerce_backend.enums.ChargebackStatus;
import com.empresa.ecommerce_backend.model.Chargeback;

public interface ChargebackRepository extends BaseRepository<Chargeback, Long> {

    List<Chargeback> findByPayment_Id(Long paymentId);

    List<Chargeback> findByStatus(ChargebackStatus status);

    Optional<Chargeback> findByProviderCaseId(String providerCaseId);
}
