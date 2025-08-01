// src/main/java/com/empresa/ecommerce_backend/service/ProductServiceImpl.java
package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.ProductRequest;
import com.empresa.ecommerce_backend.dto.response.ProductResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.exception.RecursoNoEncontradoException;
import com.empresa.ecommerce_backend.mapper.ProductMapper;
import com.empresa.ecommerce_backend.model.Product;
import com.empresa.ecommerce_backend.repository.ProductRepository;
import com.empresa.ecommerce_backend.service.interfaces.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
            return ServiceResult.error(HttpStatus.CONFLICT, "Ya existe un producto con ese SKU.");
        }

        Product entity = productMapper.toEntity(dto);
        Product saved = productRepository.save(entity);
        ProductResponse response = productMapper.toResponse(saved);
        return ServiceResult.created(response); // 201 Created
    }

    @Override
    public ServiceResult<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> list = productRepository.findAll()
                .stream()
                .map(productMapper::toResponse)
                .toList();

        return ServiceResult.ok(list); // 200 OK
    }

    @Override
    public ServiceResult<ProductResponse> getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));

        return ServiceResult.ok(productMapper.toResponse(product)); // 200 OK
    }
}
