package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.ProductPaginatedRequest;
import com.empresa.ecommerce_backend.dto.request.ProductRequest;
import com.empresa.ecommerce_backend.dto.response.PaginatedResponse;
import com.empresa.ecommerce_backend.dto.response.ProductResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.exception.RecursoNoEncontradoException;
import com.empresa.ecommerce_backend.mapper.ProductMapper;
import com.empresa.ecommerce_backend.mapper.ProductPageMapper;
import com.empresa.ecommerce_backend.model.Product;
import com.empresa.ecommerce_backend.model.ProductVariant;
import com.empresa.ecommerce_backend.repository.ProductRepository;
import com.empresa.ecommerce_backend.repository.ProductVariantRepository;
import com.empresa.ecommerce_backend.service.interfaces.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductPageMapper productPageMapper;
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional
    public ServiceResult<ProductResponse> createProduct(ProductRequest dto) {
        if (dto.getSku() != null && productRepository.existsBySku(dto.getSku())) {
            return ServiceResult.error(HttpStatus.CONFLICT, "Ya existe un producto con ese SKU base.");
        }

        Product entity = productMapper.toEntity(dto);
        Product saved = productRepository.save(entity);

        ProductResponse response = productMapper.toResponse(saved);
        return ServiceResult.created(response);
    }

    @Override
    public ServiceResult<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> list = productRepository.findAll()
                .stream()
                .map(productMapper::toResponse)
                .toList();
        return ServiceResult.ok(list);
    }

    @Override
    public ServiceResult<ProductResponse> getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));

        return ServiceResult.ok(productMapper.toResponse(product));
    }

    @Override
    public ServiceResult<PaginatedResponse<ProductResponse>> getAllProductsPaged(ProductPaginatedRequest params) {
        Pageable pageable = productPageMapper.toPageable(params);

        Page<Product> result = Boolean.TRUE.equals(params.getInStockOnly())
                ? productRepository.findInStock(pageable)  // usa query con variantes
                : productRepository.findAll(pageable);

        Page<ProductResponse> mapped = result.map(productMapper::toResponse);
        PaginatedResponse<ProductResponse> response = productPageMapper.toPaginatedResponse(mapped, params);
        return ServiceResult.ok(response);
    }
}
