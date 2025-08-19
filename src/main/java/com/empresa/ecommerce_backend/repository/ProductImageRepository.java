package com.empresa.ecommerce_backend.repository;

import com.empresa.ecommerce_backend.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    @Query("""
        select coalesce(max(pi.position),0)
        from ProductImage pi
        where pi.product.id = :productId
          and ( (:variantId is null and pi.variant is null)
             or (:variantId is not null and pi.variant.id = :variantId) )
    """)
    Optional<Integer> findMaxPosition(Long productId, Long variantId);
}
