// src/main/java/com/empresa/ecommerce_backend/repository/SupportTicketRepository.java
package com.empresa.ecommerce_backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.empresa.ecommerce_backend.enums.SupportTicketStatus;
import com.empresa.ecommerce_backend.model.SupportTicket;

public interface SupportTicketRepository extends BaseRepository<SupportTicket, Long> {

    List<SupportTicket> findByUser_IdOrderByCreatedAtDesc(Long userId);

    List<SupportTicket> findByStatus(SupportTicketStatus status);

    List<SupportTicket> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
