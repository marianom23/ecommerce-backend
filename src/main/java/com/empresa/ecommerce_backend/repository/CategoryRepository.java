// src/main/java/com/empresa/ecommerce_backend/repository/CategoryRepository.java
package com.empresa.ecommerce_backend.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.empresa.ecommerce_backend.dto.response.CategoryFacetResponse;
import org.springframework.data.jpa.repository.EntityGraph;

import com.empresa.ecommerce_backend.model.Category;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends BaseRepository<Category, Long> {

    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    List<Category> findByParent_Id(Long parentId);

    @Query(
            "select new com.empresa.ecommerce_backend.dto.response.CategoryFacetResponse(" +
                    "  c.id, c.name, c.imageUrl, count(distinct p.id)" +
                    ") " +
                    "from Category c " +
                    "left join Product p on p.category = c " +
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
                    "group by c.id, c.name " +
                    "order by c.name asc"
    )
    List<CategoryFacetResponse> findFacetsWithCounts(
            @Param("namePattern") String namePattern,
            @Param("inStockOnly") Boolean inStockOnly,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );




    @EntityGraph(attributePaths = {"children"})
    Optional<Category> findWithChildrenById(Long id);

    @EntityGraph(attributePaths = {"products"})
    Optional<Category> findWithProductsById(Long id);
}
