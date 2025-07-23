// src/main/java/com/empresa/ecommerce_backend/repository/NotificationRepository.java
package com.empresa.ecommerce_backend.repository;

import java.util.List;

import com.empresa.ecommerce_backend.model.Notification;

public interface NotificationRepository extends BaseRepository<Notification, Long> {

    List<Notification> findByUser_IdOrderBySentAtDesc(Long userId);

    long countByUser_IdAndReadFalse(Long userId);

    List<Notification> findByUser_IdAndReadFalse(Long userId);

    // Para validar acceso del usuario a la notificación
    boolean existsByIdAndUser_Id(Long id, Long userId);

    long deleteByUser_Id(Long userId);
}
