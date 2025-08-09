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

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductPageMapper {

    // Reutilizamos el mapper de Product → ProductResponse
    ProductResponse toResponse(Product product);

    // Opcional: si quieres que también mapee listas
    default List<ProductResponse> toResponseList(List<Product> products) {
        return products.stream().map(this::toResponse).toList();
    }

    // Construye el Pageable a partir del DTO (con límites y page-1)
    default Pageable toPageable(ProductPaginatedRequest p) {
        int page = Math.max(1, p.getPage()) - 1;
        int limit = Math.max(1, Math.min(100, p.getLimit()));

        // Ajusta esta lógica según tus opciones de sort
        Sort sort = "latest".equalsIgnoreCase(p.getSort())
                ? Sort.by("id").descending()
                : Sort.by(p.getSort()).ascending();

        return PageRequest.of(page, limit, sort);
    }

    // Arma la respuesta paginada completa
    default PaginatedResponse<ProductResponse> toPaginatedResponse(Page<Product> page, ProductPaginatedRequest params) {
        List<ProductResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return new PaginatedResponse<>(
                items,
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
