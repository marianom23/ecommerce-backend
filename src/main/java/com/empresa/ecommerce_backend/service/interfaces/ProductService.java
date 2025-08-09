package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.dto.request.ProductPaginatedRequest;
import com.empresa.ecommerce_backend.dto.response.PaginatedResponse;
import com.empresa.ecommerce_backend.dto.response.ProductResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.dto.request.ProductRequest;
import java.util.List;

public interface ProductService {
    ServiceResult<ProductResponse> createProduct(ProductRequest dto);
    ServiceResult<List<ProductResponse>> getAllProducts();
    ServiceResult<ProductResponse> getProductById(Long id);
    ServiceResult<PaginatedResponse<ProductResponse>> getAllProductsPaged(ProductPaginatedRequest params);

}
