// src/main/java/com/empresa/ecommerce_backend/repository/ProductRepository.java
package com.empresa.ecommerce_backend.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.empresa.ecommerce_backend.repository.projection.PriceRangeProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.empresa.ecommerce_backend.model.Product;
import org.springframework.data.repository.query.Param;

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


    @Query(
            "select min(v.price) as minPrice, max(v.price) as maxPrice " +
                    "from Product p join p.variants v " +
                    "where ( :namePattern is null or lower(p.name) like :namePattern ) " +
                    "and   ( :inStockOnly is null or :inStockOnly = false or (v.stock is not null and v.stock > 0) ) " + // <-- and
                    "and   ( :minPrice is null or v.price >= :minPrice ) " +
                    "and   ( :maxPrice is null or v.price <= :maxPrice )"
    )
    PriceRangeProjection findPriceRange(
            @Param("namePattern") String namePattern,
            @Param("inStockOnly") Boolean inStockOnly,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );

    @Query("""
        SELECT p
        FROM Product p
        LEFT JOIN p.variants v
        LEFT JOIN OrderItem oi ON oi.variant = v
        LEFT JOIN oi.order o
        WHERE (:categoryId IS NULL OR p.category.id = :categoryId)
          AND (:brandId    IS NULL OR p.brand.id    = :brandId)
          AND (:nameLike   IS NULL OR LOWER(p.name) LIKE LOWER(:nameLike))
          AND (:inStockOnly = FALSE OR EXISTS (
                 SELECT 1 FROM ProductVariant v2 WHERE v2.product = p AND v2.stock > 0
              ))
          AND (o.status IN ('PAID','SHIPPED','COMPLETED'))
          AND (o.paidAt IS NOT NULL AND o.paidAt >= :since)
        GROUP BY p.id
        ORDER BY COALESCE(SUM(oi.quantity),0) DESC, p.id DESC
        """)
    Page<Product> findBestSellingSince(
            @Param("since") LocalDateTime since,
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("nameLike") String nameLike,
            @Param("inStockOnly") boolean inStockOnly,
            Pageable pageable
    );



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
