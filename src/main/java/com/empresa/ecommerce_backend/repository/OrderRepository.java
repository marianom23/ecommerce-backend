// src/main/java/com/empresa/ecommerce_backend/repository/OrderRepository.java
package com.empresa.ecommerce_backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.empresa.ecommerce_backend.repository.projection.OrderSummaryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;

import com.empresa.ecommerce_backend.model.Order;
import com.empresa.ecommerce_backend.enums.OrderStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends BaseRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    List<Order> findByUser_IdOrderByOrderDateDesc(Long userId);

    @Query("""
      select 
         o.id as id,
         o.orderNumber as orderNumber,
         o.orderDate as orderDate,
         o.status as status,
         o.totalAmount as totalAmount,
         (select count(oi) from OrderItem oi where oi.order.id = o.id) as itemCount,
         null as firstItemThumb
      from Order o
      where o.user.id = :userId
      """)
    Page<OrderSummaryProjection> findSummariesByUserId(@Param("userId") Long userId, Pageable pageable);

    Optional<Order> findByOrderNumberAndUserId(String orderNumber, Long userId);

    Optional<Order> findByIdAndUserId(Long id, Long userId);

    List<Order> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByOrderDateBetween(LocalDateTime from, LocalDateTime to);

    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<Order> findWithItemsById(Long id);
}
