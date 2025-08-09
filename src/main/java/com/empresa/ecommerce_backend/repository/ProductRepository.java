// src/main/java/com/empresa/ecommerce_backend/repository/ProductRepository.java
package com.empresa.ecommerce_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import com.empresa.ecommerce_backend.model.Product;

public interface ProductRepository extends BaseRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Product> findByCategory_Id(Long categoryId);

    List<Product> findByBrand_Id(Long brandId);

    // Buscar por nombre (case-insensitive, contiene)
    List<Product> findByNameContainingIgnoreCase(String namePart);

    // Con descuentos/tags eager usando EntityGraph
    @EntityGraph(attributePaths = {"discounts", "tags"})
    Optional<Product> findWithDiscountsAndTagsById(Long id);

    // Ejemplo: productos con stock bajo
    List<Product> findByStockLessThan(Integer threshold);

    // Ejemplo JPQL para traer precio promedio por categoría (por si lo usas)
    @Query("select avg(p.price) from Product p where p.category.id = :categoryId")
    Double avgPriceByCategory(Long categoryId);

    Page<Product> findByStockGreaterThan(int stock, Pageable pageable);
}
