// src/main/java/com/empresa/ecommerce_backend/service/ProductServiceImpl.java
package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.request.ProductPaginatedRequest;
import com.empresa.ecommerce_backend.dto.request.ProductRequest;
import com.empresa.ecommerce_backend.dto.response.PaginatedResponse;
import com.empresa.ecommerce_backend.dto.response.ProductResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.enums.StockTrackingMode;
import com.empresa.ecommerce_backend.exception.RecursoNoEncontradoException;
import com.empresa.ecommerce_backend.mapper.ProductMapper;
import com.empresa.ecommerce_backend.mapper.ProductPageMapper;
import com.empresa.ecommerce_backend.model.Product;
import com.empresa.ecommerce_backend.repository.ProductRepository;
import com.empresa.ecommerce_backend.repository.ProductVariantRepository;
import com.empresa.ecommerce_backend.service.interfaces.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductPageMapper productPageMapper;
    private final ProductVariantRepository productVariantRepository; // <-- inyectar

    @Override
    public ServiceResult<ProductResponse> createProduct(ProductRequest dto) {
        if (dto.getSku() != null && productRepository.existsBySku(dto.getSku())) {
            return ServiceResult.error(HttpStatus.CONFLICT, "Ya existe un producto con ese SKU.");
        }

        Product entity = productMapper.toEntity(dto);
        entity.setStockTrackingMode(StockTrackingMode.SIMPLE); // <-- por defecto

        Product saved = productRepository.save(entity);
        ProductResponse response = productMapper.toResponse(saved);
        // opcional: si querés setear stock efectivo en la respuesta:
        // response.setStock(saved.getStock());

        return ServiceResult.created(response);
    }

    @Override
    public ServiceResult<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> list = productRepository.findAll()
                .stream()
                .map(p -> {
                    ProductResponse r = productMapper.toResponse(p);
                    // opcional: stock efectivo
                    // if (p.getStockTrackingMode() == StockTrackingMode.VARIANT) {
                    //     r.setStock(productVariantRepository.sumStockByProductId(p.getId()));
                    // } else {
                    //     r.setStock(p.getStock());
                    // }
                    return r;
                })
                .toList();

        return ServiceResult.ok(list);
    }

    @Override
    public ServiceResult<ProductResponse> getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));

        ProductResponse r = productMapper.toResponse(product);
        // opcional: stock efectivo
        // if (product.getStockTrackingMode() == StockTrackingMode.VARIANT) {
        //     r.setStock(productVariantRepository.sumStockByProductId(product.getId()));
        // } else {
        //     r.setStock(product.getStock());
        // }
        return ServiceResult.ok(r);
    }

    @Override
    public ServiceResult<PaginatedResponse<ProductResponse>> getAllProductsPaged(ProductPaginatedRequest params) {
        Pageable pageable = productPageMapper.toPageable(params);

        Page<Product> result = Boolean.TRUE.equals(params.getInStockOnly())
                ? productRepository.findInStock(pageable)          // 👈 acá
                : productRepository.findAll(pageable);

        Page<ProductResponse> mapped = result.map(productMapper::toResponse);
        PaginatedResponse<ProductResponse> response = productPageMapper.toPaginatedResponse(mapped, params);
        return ServiceResult.ok(response);
    }


}
