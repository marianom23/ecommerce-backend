// src/main/java/com/empresa/ecommerce_backend/repository/ProductRepository.java
package com.empresa.ecommerce_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.empresa.ecommerce_backend.model.Product;

public interface ProductRepository extends BaseRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Query(
            value = """
          select p
          from Product p
          where exists (
              select 1
              from ProductVariant v
              where v.product = p
                and coalesce(v.stock, 0) > 0
          )
        """,
            countQuery = """
          select count(p)
          from Product p
          where exists (
              select 1
              from ProductVariant v
              where v.product = p
                and coalesce(v.stock, 0) > 0
          )
        """
    )
    Page<Product> findInStock(Pageable pageable);

    Optional<Product> findBySku(String sku);
    boolean existsBySku(String sku);
    List<Product> findByCategory_Id(Long categoryId);
    List<Product> findByBrand_Id(Long brandId);

    @EntityGraph(attributePaths = { "images", "brand", "category", "discounts", "tags" })
    Optional<Product> findWithDetailsById(Long id);

    List<Product> findByNameContainingIgnoreCase(String namePart);

    @EntityGraph(attributePaths = {"discounts", "tags"})
    Optional<Product> findWithDiscountsAndTagsById(Long id);
}
