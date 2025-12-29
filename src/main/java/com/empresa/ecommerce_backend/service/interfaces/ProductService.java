package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.dto.request.ProductPaginatedRequest;
import com.empresa.ecommerce_backend.dto.response.*;
import com.empresa.ecommerce_backend.dto.request.ProductRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    ServiceResult<ProductResponse> createProduct(ProductRequest dto);
    ServiceResult<List<ProductResponse>> getAllProducts();
    ServiceResult<ProductResponse> getProductById(Long id);
    ServiceResult<PaginatedResponse<ProductResponse>> getAllProductsPaged(ProductPaginatedRequest params);
    ServiceResult<ProductFacetsResponse> getProductFacets(ProductPaginatedRequest params);
    
    // Admin backoffice
    ServiceResult<PageResponse<ProductBackofficeResponse>> listForBackoffice(Pageable pageable, String searchQuery);
}
