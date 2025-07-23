// src/main/java/com/empresa/ecommerce_backend/repository/RefundRepository.java
package com.empresa.ecommerce_backend.repository;

import java.util.List;
import java.util.Optional;

import com.empresa.ecommerce_backend.enums.RefundStatus;
import com.empresa.ecommerce_backend.model.Refund;

public interface RefundRepository extends BaseRepository<Refund, Long> {

    List<Refund> findByPayment_Id(Long paymentId);

    List<Refund> findByStatus(RefundStatus status);

    Optional<Refund> findByProviderRefundId(String providerRefundId);
}
