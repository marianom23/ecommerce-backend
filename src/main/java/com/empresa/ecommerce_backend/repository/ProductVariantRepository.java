// src/main/java/com/empresa/ecommerce_backend/repository/ProductVariantRepository.java
package com.empresa.ecommerce_backend.repository;

import com.empresa.ecommerce_backend.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    Optional<ProductVariant> findByIdAndProductId(Long id, Long productId);
    List<ProductVariant> findAllByProductId(Long productId);
    boolean existsBySku(String sku);
    boolean existsByProductId(Long productId);
    long countByProductId(Long productId);
}
