package com.empresa.ecommerce_backend.repository;

import com.empresa.ecommerce_backend.enums.AttributeScope;
import com.empresa.ecommerce_backend.enums.ProductType;
import com.empresa.ecommerce_backend.model.AttributeTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttributeTemplateRepository extends JpaRepository<AttributeTemplate, Long> {

    @Query("SELECT t FROM AttributeTemplate t LEFT JOIN FETCH t.values " +
           "WHERE t.scope = :scope " +
           "AND (t.productType IS NULL OR t.productType = :productType) " +
           "AND (t.category.id IS NULL OR t.category.id = :categoryId)")
    List<AttributeTemplate> findApplicable(
            @Param("scope") AttributeScope scope,
            @Param("productType") ProductType productType,
            @Param("categoryId") Long categoryId
    );

    List<AttributeTemplate> findByScope(AttributeScope scope);
}
