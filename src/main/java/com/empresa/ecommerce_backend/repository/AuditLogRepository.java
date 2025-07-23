// src/main/java/com/empresa/ecommerce_backend/repository/AuditLogRepository.java
package com.empresa.ecommerce_backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.empresa.ecommerce_backend.model.AuditLog;

public interface AuditLogRepository extends BaseRepository<AuditLog, Long> {

    List<AuditLog> findByUser_IdOrderByTimestampDesc(Long userId);

    List<AuditLog> findByEntityAndEntityIdOrderByTimestampDesc(String entity, Long entityId);

    List<AuditLog> findByActionOrderByTimestampDesc(String action);

    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime from, LocalDateTime to);
}
