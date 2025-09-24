package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.dto.request.ProductPaginatedRequest;
import com.empresa.ecommerce_backend.dto.response.PaginatedResponse;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


@Mapper(componentModel = "spring")
public interface ProductPageMapper {

    default Pageable toPageable(ProductPaginatedRequest p) {
        // defensivo por si p viene null (depende de tu controller)
        if (p == null) {
            return PageRequest.of(0, 12, Sort.by("id").descending());
        }

        int page = Math.max(1, p.getPage()) - 1;
        int limit = Math.max(1, Math.min(100, p.getLimit()));
        String sortKey = (p.getSort() == null) ? "latest" : p.getSort().trim();

        // Sorts "especiales" que se resuelven con query dedicada en el servicio/repositorio
        if ("bestSellingWeek".equalsIgnoreCase(sortKey) ||
                "bestSellingSince".equalsIgnoreCase(sortKey)) {
            // la query ya tiene su ORDER BY (SUM qty desc), no metas otro sort
            return PageRequest.of(page, limit);
        }

        // Sorts por campo/índice directo
        Sort sort = switch (sortKey.toLowerCase()) {
            case "latest"       -> Sort.by("id").descending();
            case "bestselling"  -> Sort.by("soldCount").descending().and(Sort.by("id").descending());
            case "id"           -> Sort.by("id").ascending(); // "Old Products" en tu UI
            default             -> Sort.by("id").descending(); // fallback razonable
        };

        return PageRequest.of(page, limit, sort);
    }

    // Genérico: sirve para cualquier Page<T>
    default <T> PaginatedResponse<T> toPaginatedResponse(Page<T> page, ProductPaginatedRequest params) {
        return new PaginatedResponse<>(
                page.getContent(),
                page.getTotalElements(),
                params.getPage(),
                params.getLimit(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious(),
                params.getSort(),
                params.getQ()
        );
    }
}
