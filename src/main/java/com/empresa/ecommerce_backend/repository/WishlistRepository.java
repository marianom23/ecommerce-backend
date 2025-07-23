// src/main/java/com/empresa/ecommerce_backend/repository/WishlistRepository.java
package com.empresa.ecommerce_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;

import com.empresa.ecommerce_backend.model.Wishlist;

public interface WishlistRepository extends BaseRepository<Wishlist, Long> {

    List<Wishlist> findByUser_IdOrderByNameAsc(Long userId);

    Optional<Wishlist> findByIdAndUser_Id(Long id, Long userId);

    boolean existsByUser_IdAndName(Long userId, String name);

    @EntityGraph(attributePaths = "products")
    Optional<Wishlist> findWithProductsById(Long id);

    long deleteByIdAndUser_Id(Long id, Long userId);
}
