// src/main/java/com/empresa/ecommerce_backend/repository/ReturnRequestRepository.java
package com.empresa.ecommerce_backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.empresa.ecommerce_backend.enums.ReturnRequestStatus;
import com.empresa.ecommerce_backend.model.ReturnRequest;

public interface ReturnRequestRepository extends BaseRepository<ReturnRequest, Long> {

    List<ReturnRequest> findByUser_IdOrderByRequestedAtDesc(Long userId);

    List<ReturnRequest> findByOrder_Id(Long orderId);

    List<ReturnRequest> findByStatus(ReturnRequestStatus status);

    List<ReturnRequest> findByRequestedAtBetween(LocalDateTime from, LocalDateTime to);

    Optional<ReturnRequest> findByIdAndUser_Id(Long id, Long userId);
}
