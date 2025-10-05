// src/main/java/com/empresa/ecommerce_backend/repository/CartRepository.java
package com.empresa.ecommerce_backend.repository;

import java.util.Optional;

import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;

import com.empresa.ecommerce_backend.model.Cart;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface CartRepository extends BaseRepository<Cart, Long> {

    Optional<Cart> findBySessionId(String sessionId);

    Optional<Cart> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cart c where c.sessionId = :sessionId")
    Optional<Cart> lockBySessionId(@Param("sessionId") String sessionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cart c where c.user.id = :userId")
    Optional<Cart> lockByUserId(@Param("userId") Long userId);
}
