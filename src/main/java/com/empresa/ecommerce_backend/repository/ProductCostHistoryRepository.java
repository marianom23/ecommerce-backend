package com.empresa.ecommerce_backend.repository;

import com.empresa.ecommerce_backend.model.ProductCostHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCostHistoryRepository extends JpaRepository<ProductCostHistory, Long> {

    List<ProductCostHistory> findByProduct_IdOrderByEffectiveFromDesc(Long productId);

    // Último costo vigente de un producto
    Optional<ProductCostHistory> findTopByProduct_IdOrderByEffectiveFromDesc(Long productId);

    // Costo vigente a una fecha específica
    Optional<ProductCostHistory> findTopByProduct_IdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(Long productId, LocalDateTime date);
}
