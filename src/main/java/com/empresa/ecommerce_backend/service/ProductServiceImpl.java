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
import com.empresa.ecommerce_backend.repository.ProductRepository;
import com.empresa.ecommerce_backend.repository.ProductVariantRepository;
import com.empresa.ecommerce_backend.repository.spec.ProductSpecs;
import com.empresa.ecommerce_backend.service.interfaces.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.jpa.domain.Specification.allOf;

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
        var entity = productMapper.toEntity(dto);
        var saved = productRepository.save(entity);
        return ServiceResult.created(productMapper.toResponse(saved));
    }

    @Override
    public ServiceResult<List<ProductResponse>> getAllProducts() {
        var list = productRepository.findAll()
                .stream()
                .map(productMapper::toResponse)
                .toList();
        return ServiceResult.ok(list);
    }

    @Override
    public ServiceResult<ProductResponse> getProductById(Long id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
        return ServiceResult.ok(productMapper.toResponse(product));
    }

    @Override
    public ServiceResult<PaginatedResponse<ProductResponse>> getAllProductsPaged(ProductPaginatedRequest params) {
        Pageable pageable = productPageMapper.toPageable(params);
        // Si querés forzar sort acá en vez de en el mapper:
        // Pageable pageable = PageRequest.of(Math.max(0, params.getPage() - 1), params.getLimit(), resolveSort(params.getSort()));

        // Armar specs sin nulls
        List<Specification<Product>> parts = new ArrayList<>();
        if (Boolean.TRUE.equals(params.getInStockOnly())) parts.add(ProductSpecs.inStockOnly(true));
        if (params.getCategoryId() != null) parts.add(ProductSpecs.hasCategory(params.getCategoryId()));
        if (params.getBrandId() != null) parts.add(ProductSpecs.hasBrand(params.getBrandId()));
        if (params.getQ() != null && !params.getQ().isBlank()) parts.add(ProductSpecs.nameContains(params.getQ()));
        if (params.getMinPrice() != null || params.getMaxPrice() != null)
            parts.add(ProductSpecs.priceBetween(params.getMinPrice(), params.getMaxPrice()));
        if (params.getColors() != null && !params.getColors().isEmpty())
            parts.add(ProductSpecs.colorsIn(params.getColors()));
        if (params.getSizes() != null && !params.getSizes().isEmpty())
            parts.add(ProductSpecs.sizesIn(params.getSizes()));
        if (params.getTags() != null && !params.getTags().isEmpty())
            parts.add(ProductSpecs.tagsIn(params.getTags()));

        Specification<Product> spec = parts.isEmpty() ? Specification.allOf() : allOf(parts);

        Page<Product> result = productRepository.findAll(spec, pageable);
        Page<ProductResponse> mapped = result.map(productMapper::toResponse);
        var response = productPageMapper.toPaginatedResponse(mapped, params);
        return ServiceResult.ok(response);
    }

    private Sort resolveSort(String sortKey) {
        if (sortKey == null) sortKey = "latest";
        return switch (sortKey) {
            case "latest" -> Sort.by(Sort.Direction.DESC, "id");
            case "bestSelling" -> Sort.by(Sort.Direction.DESC, "id"); // TODO: cambiar a soldCount si lo agregás
            case "id" -> Sort.by(Sort.Direction.ASC, "id");
            default -> Sort.by(Sort.Direction.DESC, "id");
        };
    }
}
