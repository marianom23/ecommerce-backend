// src/main/java/com/empresa/ecommerce_backend/repository/BrandRepository.java
package com.empresa.ecommerce_backend.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.empresa.ecommerce_backend.dto.response.BrandFacetResponse;
import org.springframework.data.jpa.repository.EntityGraph;

import com.empresa.ecommerce_backend.model.Brand;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BrandRepository extends BaseRepository<Brand, Long> {

    Optional<Brand> findByName(String name);

    boolean existsByName(String name);

    @Query(
            "select new com.empresa.ecommerce_backend.dto.response.BrandFacetResponse(" +
                    "  b.id, b.name, count(distinct p.id)" +
                    ") " +
                    "from Brand b " +
                    "left join Product p on p.brand = b " +
                    "where " +
                    "  ( :namePattern is null or lower(p.name) like :namePattern ) " +
                    "  and ( p is null " +
                    "        or exists ( " +
                    "             select 1 from ProductVariant v " +
                    "             where v.product = p " +
                    "               and ( :inStockOnly is null or :inStockOnly = false or (v.stock is not null and v.stock > 0) ) " + // <-- and
                    "               and ( :minPrice is null or v.price >= :minPrice ) " +
                    "               and ( :maxPrice is null or v.price <= :maxPrice ) " +
                    "        ) " +
                    "      ) " +
                    "group by b.id, b.name " +
                    "order by b.name asc"
    )
    List<BrandFacetResponse> findFacetsWithCounts(
            @Param("namePattern") String namePattern,
            @Param("inStockOnly") Boolean inStockOnly,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );




    List<Brand> findByNameContainingIgnoreCase(String partialName);

    @EntityGraph(attributePaths = "products")
    Optional<Brand> findWithProductsById(Long id);
}
