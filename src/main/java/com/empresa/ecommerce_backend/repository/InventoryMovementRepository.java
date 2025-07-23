// src/main/java/com/empresa/ecommerce_backend/repository/InventoryMovementRepository.java
package com.empresa.ecommerce_backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.empresa.ecommerce_backend.enums.InventoryMovementType;
import com.empresa.ecommerce_backend.model.InventoryMovement;

public interface InventoryMovementRepository extends BaseRepository<InventoryMovement, Long> {

    List<InventoryMovement> findByProduct_IdOrderByMovementDateDesc(Long productId);

    List<InventoryMovement> findByProduct_IdAndMovementDateBetween(Long productId, LocalDateTime from, LocalDateTime to);

    List<InventoryMovement> findByType(InventoryMovementType type);

    List<InventoryMovement> findByUser_Id(Long userId);
}
