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

    // ✔️ Opción 1: order + variant (coincide con tu unique constraint uk_order_variant)
    Optional<OrderItem> findByOrderIdAndVariantId(Long orderId, Long variantId);

    long deleteByOrderIdAndVariantId(Long orderId, Long variantId);

    long deleteByOrder_Id(Long orderId);
}
