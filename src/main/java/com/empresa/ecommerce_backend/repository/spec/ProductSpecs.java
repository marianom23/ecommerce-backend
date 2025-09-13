// src/main/java/com/empresa/ecommerce_backend/repository/spec/ProductSpecs.java
package com.empresa.ecommerce_backend.repository.spec;

import com.empresa.ecommerce_backend.model.Product;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.JoinType;
import java.math.BigDecimal;
import java.util.List;

public class ProductSpecs {

    public static Specification<Product> inStockOnly(Boolean flag) {
        if (!Boolean.TRUE.equals(flag)) return null;
        return (root, query, cb) -> {
            query.distinct(true);
            var v = root.join("variants", JoinType.INNER);
            return cb.greaterThan(cb.coalesce(v.get("stock"), 0), 0);
        };
    }

    public static Specification<Product> hasCategory(Long categoryId) {
        if (categoryId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> hasBrand(Long brandId) {
        if (brandId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("brand").get("id"), brandId);
    }

    public static Specification<Product> nameContains(String q) {
        if (q == null || q.isBlank()) return null;
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + q.toLowerCase() + "%");
    }

    // precio está en ProductVariant.price
    public static Specification<Product> priceBetween(BigDecimal min, BigDecimal max) {
        if (min == null && max == null) return null;
        return (root, query, cb) -> {
            query.distinct(true);
            var v = root.join("variants", JoinType.INNER);
            if (min != null && max != null) return cb.between(v.get("price"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(v.get("price"), min);
            return cb.lessThanOrEqualTo(v.get("price"), max);
        };
    }

    // attributesJson en variante: buscamos por LIKE case-insensitive
    // Ej: "color":"Red" o "size":"M"
    private static Specification<Product> variantAttrLike(String key, List<String> values) {
        if (values == null || values.isEmpty()) return null;
        return (root, query, cb) -> {
            query.distinct(true);
            var v = root.join("variants", JoinType.INNER);
            var attr = cb.lower(v.get("attributesJson"));
            // patrón JSON simple. Si usás JSON nativo de tu DB, podés reemplazar por JSON_EXTRACT con CriteriaBuilder#function
            var predicates = values.stream()
                    .filter(s -> s != null && !s.isBlank())
                    .map(s -> cb.like(attr, "%\"" + key.toLowerCase() + "\":\"" + s.toLowerCase() + "\"%"))
                    .toArray(jakarta.persistence.criteria.Predicate[]::new);
            return cb.or(predicates);
        };
    }

    public static Specification<Product> colorsIn(List<String> colors) {
        return variantAttrLike("color", colors);
    }

    public static Specification<Product> sizesIn(List<String> sizes) {
        return variantAttrLike("size", sizes);
    }

    // tags de producto (ManyToMany)
    public static Specification<Product> tagsIn(List<String> tags) {
        if (tags == null || tags.isEmpty()) return null;
        return (root, query, cb) -> {
            query.distinct(true);
            var t = root.join("tags", JoinType.INNER);
            CriteriaBuilder.In<String> in = cb.in(cb.lower(t.get("name")));
            tags.stream().filter(s -> s != null && !s.isBlank())
                    .map(String::toLowerCase).forEach(in::value);
            return in;
        };
    }
}
