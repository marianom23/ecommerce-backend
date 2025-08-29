// src/main/java/com/empresa/ecommerce_backend/repository/ProductVariantRepository.java
package com.empresa.ecommerce_backend.repository;

import com.empresa.ecommerce_backend.model.ProductVariant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    Optional<ProductVariant> findByIdAndProductId(Long id, Long productId);
    List<ProductVariant> findAllByProductId(Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from ProductVariant v join fetch v.product where v.id = :id")
    ProductVariant lockByIdWithProduct(@Param("id") Long id);

    @EntityGraph(attributePaths = "images")
    List<ProductVariant> findAllByProductIdOrderByIdAsc(Long productId);

    boolean existsBySku(String sku);
    boolean existsByProductId(Long productId);
    long countByProductId(Long productId);
}
