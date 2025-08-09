package com.empresa.ecommerce_backend.mapper;

import com.empresa.ecommerce_backend.dto.request.ProductPaginatedRequest;
import com.empresa.ecommerce_backend.dto.response.PaginatedResponse;
import com.empresa.ecommerce_backend.dto.response.ProductResponse;
import com.empresa.ecommerce_backend.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductPageMapper {

    default Pageable toPageable(ProductPaginatedRequest p) {
        int page = Math.max(1, p.getPage()) - 1;
        int limit = Math.max(1, Math.min(100, p.getLimit()));
        String sortField = (p.getSort() == null || p.getSort().isBlank()) ? "id" : p.getSort();
        Sort sort = "latest".equalsIgnoreCase(sortField)
                ? Sort.by("id").descending()
                : Sort.by(sortField).ascending();
        return PageRequest.of(page, limit, sort);
    }

    // <-- genérico: sirve para cualquier Page<T>
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
