package com.empresa.ecommerce_backend.repository;

import com.empresa.ecommerce_backend.dto.response.ConsoleFacetResponse;
import com.empresa.ecommerce_backend.model.Console;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsoleRepository extends JpaRepository<Console, Long> {
    Optional<Console> findByName(String name);

    @Query(
            "select new com.empresa.ecommerce_backend.dto.response.ConsoleFacetResponse(" +
                    "  c.id, c.name, c.imageUrl, count(distinct p.id)" +
                    ") " +
                    "from Console c " +
                    "left join Product p on p.console = c " +
                    "where " +
                    "  ( :namePattern is null or lower(p.name) like :namePattern ) " +
                    "  and ( p is null " +
                    "        or exists ( " +
                    "             select 1 from ProductVariant v " +
                    "             where v.product = p " +
                    "               and ( :inStockOnly is null or :inStockOnly = false or (v.stock is not null and v.stock > 0) ) " +
                    "               and ( :minPrice is null or v.price >= :minPrice ) " +
                    "               and ( :maxPrice is null or v.price <= :maxPrice ) " +
                    "        ) " +
                    "      ) " +
                    "group by c.id, c.name " +
                    "order by c.name asc"
    )
    List<ConsoleFacetResponse> findFacetsWithCounts(
            @Param("namePattern") String namePattern,
            @Param("inStockOnly") Boolean inStockOnly,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );
}
