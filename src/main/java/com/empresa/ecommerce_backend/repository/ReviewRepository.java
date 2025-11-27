// src/main/java/com/empresa/ecommerce_backend/repository/ReviewRepository.java
package com.empresa.ecommerce_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;

import com.empresa.ecommerce_backend.model.Review;

public interface ReviewRepository extends BaseRepository<Review, Long> {

    List<Review> findByProduct_IdOrderByReviewDateDesc(Long productId);

    List<Review> findByUser_IdOrderByReviewDateDesc(Long userId);

    Optional<Review> findByUser_IdAndProduct_Id(Long userId, Long productId);

    boolean existsByUser_IdAndProduct_Id(Long userId, Long productId);

    @Query("select avg(r.rating) from Review r where r.product.id = :productId")
    Double averageRatingByProduct(Long productId);

    long countByProduct_Id(Long productId);

    List<Review> findByRatingGreaterThanEqualOrderByReviewDateDesc(Integer rating, org.springframework.data.domain.Pageable pageable);
}
