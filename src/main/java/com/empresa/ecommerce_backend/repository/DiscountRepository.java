// src/main/java/com/empresa/ecommerce_backend/repository/DiscountRepository.java
package com.empresa.ecommerce_backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;

import com.empresa.ecommerce_backend.model.Discount;

public interface DiscountRepository extends BaseRepository<Discount, Long> {

    Optional<Discount> findByName(String name);

    boolean existsByName(String name);

    // Descuentos activos en un instante
    @Query("""
           select d from Discount d
           where (d.startDate is null or d.startDate <= :now)
             and (d.endDate   is null or d.endDate   >= :now)
           """)
    List<Discount> findActiveAt(LocalDateTime now);

    // Por producto
    List<Discount> findByProducts_Id(Long productId);
}
