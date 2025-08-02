// src/main/java/com/empresa/ecommerce_backend/repository/ProductVariantRepository.java
package com.empresa.ecommerce_backend.repository;

import com.empresa.ecommerce_backend.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    Optional<ProductVariant> findByIdAndProductId(Long id, Long productId);

    boolean existsByProductId(Long productId);
}
