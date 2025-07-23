// repository/ProductImageRepository.java
package com.empresa.ecommerce_backend.repository;

import java.util.List;

import com.empresa.ecommerce_backend.model.ProductImage;

public interface ProductImageRepository extends BaseRepository<ProductImage, Long> {
    List<ProductImage> findByProduct_IdOrderByPositionAsc(Long productId);
}
