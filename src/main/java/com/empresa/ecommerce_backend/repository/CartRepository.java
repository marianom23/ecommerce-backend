// src/main/java/com/empresa/ecommerce_backend/repository/CartRepository.java
package com.empresa.ecommerce_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;

import com.empresa.ecommerce_backend.model.Cart;

public interface CartRepository extends BaseRepository<Cart, Long> {

    Optional<Cart> findByUser_Id(Long userId);
    Optional<Cart> findBySessionId(String sessionId);
    boolean existsByUser_Id(Long userId);

    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<Cart> findWithItemsByUser_Id(Long userId);
}
