// src/main/java/com/empresa/ecommerce_backend/dto/request/ProductPaginatedRequest.java
package com.empresa.ecommerce_backend.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductPaginatedRequest {
    // paginación (tu mapper ya lo convierte a Pageable)
    @Min(1)
    private Integer page = 1;

    @Min(1)
    private Integer limit = 12;

    // orden
    // "latest" | "bestSelling" | "id"
    private String sort;        // "latest", "bestSelling", "bestSellingWeek", ...
    private Integer sinceDays;  // ej: 7, 30, 90 (opcional)

    // filtros
    private Boolean inStockOnly = false;

    private Long categoryId;
    private Long brandId;

    // búsqueda por nombre
    private String q;

    // precio (en VARIANTE)
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    // atributos de variante; en tu modelo están en attributesJson
    // Para simplicidad, los pasamos como listas de strings
    private List<String> colors;
    private List<String> sizes;

    // tags de producto
    private List<String> tags;
}
