// src/main/java/com/empresa/ecommerce_backend/repository/OrderItemRepository.java
package com.empresa.ecommerce_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;

import com.empresa.ecommerce_backend.model.OrderItem;

public interface OrderItemRepository extends BaseRepository<OrderItem, Long> {

    List<OrderItem> findByOrder_Id(Long orderId);

    @EntityGraph(attributePaths = {"product"})
    List<OrderItem> findWithProductByOrder_Id(Long orderId);

    Optional<OrderItem> findByOrder_IdAndProduct_Id(Long orderId, Long productId);

    long deleteByOrder_Id(Long orderId);
}
