// src/main/java/com/empresa/ecommerce_backend/repository/BankTransferRepository.java
package com.empresa.ecommerce_backend.repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.empresa.ecommerce_backend.model.BankTransfer;
import com.empresa.ecommerce_backend.enums.BankTransferStatus;

public interface BankTransferRepository extends BaseRepository<BankTransfer, Long> {

    Optional<BankTransfer> findByTransactionNumber(String transactionNumber);

    List<BankTransfer> findByOrder_Id(Long orderId);

    List<BankTransfer> findByUser_Id(Long userId);

    List<BankTransfer> findByStatus(BankTransferStatus status);

    List<BankTransfer> findByTransferDateBetween(LocalDateTime from, LocalDateTime to);

    boolean existsByTransactionNumber(String transactionNumber);
}
