// src/main/java/com/empresa/ecommerce_backend/repository/CartItemRepository.java
package com.empresa.ecommerce_backend.repository;

import java.util.List;
import java.util.Optional;

import com.empresa.ecommerce_backend.model.CartItem;

public interface CartItemRepository extends BaseRepository<CartItem, Long> {

    List<CartItem> findByCart_Id(Long cartId);

    Optional<CartItem> findByCart_IdAndProduct_Id(Long cartId, Long productId);

    boolean existsByCart_IdAndProduct_Id(Long cartId, Long productId);

    long deleteByCart_IdAndProduct_Id(Long cartId, Long productId);

    long deleteByCart_Id(Long cartId);
}
