// repository/ProductVariantRepository.java
package com.empresa.ecommerce_backend.repository;

import java.util.List;
import java.util.Optional;

import com.empresa.ecommerce_backend.model.ProductVariant;

public interface ProductVariantRepository extends BaseRepository<ProductVariant, Long> {
    List<ProductVariant> findByProduct_Id(Long productId);
    Optional<ProductVariant> findBySku(String sku);
    boolean existsBySku(String sku);
}
