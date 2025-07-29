package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.ProductRequest;
import com.empresa.ecommerce_backend.dto.response.ProductResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.mapper.ProductMapper;
import com.empresa.ecommerce_backend.model.Product;
import com.empresa.ecommerce_backend.repository.ProductRepository;
import com.empresa.ecommerce_backend.service.interfaces.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public ServiceResult<ProductResponse> createProduct(ProductRequest dto) {
        if (dto.getSku() != null && productRepository.existsBySku(dto.getSku())) {
            return ServiceResult.failure("Ya existe un producto con ese SKU.");
        }

        Product entity = productMapper.toEntity(dto);
        Product saved = productRepository.save(entity);
        ProductResponse response = productMapper.toResponse(saved);
        return ServiceResult.success(response);
    }

    @Override
    public ServiceResult<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> list = productRepository.findAll()
                .stream()
                .map(productMapper::toResponse)
                .toList();
        return ServiceResult.success(list);
    }

    @Override
    public ServiceResult<ProductResponse> getProductById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toResponse)
                .map(ServiceResult::success)
                .orElseGet(() -> ServiceResult.failure("Producto no encontrado"));
    }
}