// src/main/java/com/empresa/ecommerce_backend/repository/PaymentCardRepository.java
package com.empresa.ecommerce_backend.repository;

import java.util.List;
import java.util.Optional;

import com.empresa.ecommerce_backend.model.PaymentCard;

public interface PaymentCardRepository extends BaseRepository<PaymentCard, Long> {

    List<PaymentCard> findByUser_Id(Long userId);

    Optional<PaymentCard> findByIdAndUser_Id(Long id, Long userId);

    Optional<PaymentCard> findByGatewayToken(String gatewayToken);

    boolean existsByUser_IdAndGatewayToken(Long userId, String gatewayToken);
}
