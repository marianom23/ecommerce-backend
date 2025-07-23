// src/main/java/com/empresa/ecommerce_backend/repository/BrandRepository.java
package com.empresa.ecommerce_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;

import com.empresa.ecommerce_backend.model.Brand;

public interface BrandRepository extends BaseRepository<Brand, Long> {

    Optional<Brand> findByName(String name);

    boolean existsByName(String name);

    List<Brand> findByNameContainingIgnoreCase(String partialName);

    @EntityGraph(attributePaths = "products")
    Optional<Brand> findWithProductsById(Long id);
}
