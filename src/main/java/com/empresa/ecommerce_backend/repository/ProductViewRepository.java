// repository/ProductViewRepository.java
package com.empresa.ecommerce_backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.empresa.ecommerce_backend.model.ProductView;

public interface ProductViewRepository extends BaseRepository<ProductView, Long> {

    List<ProductView> findByUser_IdOrderByViewedAtDesc(Long userId);

    List<ProductView> findByProduct_IdAndViewedAtBetween(Long productId, LocalDateTime from, LocalDateTime to);
}
