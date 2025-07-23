// src/main/java/com/empresa/ecommerce_backend/repository/CategoryRepository.java
package com.empresa.ecommerce_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;

import com.empresa.ecommerce_backend.model.Category;

public interface CategoryRepository extends BaseRepository<Category, Long> {

    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    List<Category> findByParent_Id(Long parentId);

    @EntityGraph(attributePaths = {"children"})
    Optional<Category> findWithChildrenById(Long id);

    @EntityGraph(attributePaths = {"products"})
    Optional<Category> findWithProductsById(Long id);
}
