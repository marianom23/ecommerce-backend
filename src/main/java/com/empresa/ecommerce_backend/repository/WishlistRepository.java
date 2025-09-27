// src/main/java/com/empresa/ecommerce_backend/repository/WishlistRepository.java
package com.empresa.ecommerce_backend.repository;

import com.empresa.ecommerce_backend.model.Wishlist;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface WishlistRepository extends BaseRepository<Wishlist, Long> {

    Optional<Wishlist> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wishlist w where w.user.id = :userId")
    Optional<Wishlist> lockByUserId(Long userId);
}
