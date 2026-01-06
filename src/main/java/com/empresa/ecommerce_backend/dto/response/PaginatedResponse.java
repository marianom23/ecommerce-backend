package com.empresa.ecommerce_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> items;
    private long total;         // total de elementos en la BD (sin paginar)
    private int page;           // página actual (1-based)
    private int pageSize;       // tamaño de página solicitada
    private int totalPages;     // total de páginas
    private boolean hasNext;    // hay más páginas después
    private boolean hasPrevious;// hay páginas antes
    private String sort;        // criterio de orden actual (opcional)
    private String query;       // texto de búsqueda (opcional)

    public PaginatedResponse(List<T> items, int page, int pageSize, long total, int totalPages) {
        this.items = items;
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
        this.totalPages = totalPages;
        this.hasNext = page < totalPages;
        this.hasPrevious = page > 1;
    }
}
