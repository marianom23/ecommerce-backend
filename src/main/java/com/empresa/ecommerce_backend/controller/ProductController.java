// src/main/java/com/empresa/ecommerce_backend/controller/ProductController.java
package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.request.ProductPaginatedRequest;
import com.empresa.ecommerce_backend.dto.request.ProductRequest;
import com.empresa.ecommerce_backend.dto.response.*;
import com.empresa.ecommerce_backend.service.interfaces.ProductDetailsService;
import com.empresa.ecommerce_backend.service.interfaces.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductDetailsService productDetailsService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ServiceResult<ProductResponse> createProduct(@RequestBody @Valid ProductRequest dto) {
        return productService.createProduct(dto); // -> 201 Created (ServiceResult.created)
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ServiceResult<ProductResponse> updateProduct(
            @PathVariable Long id,
            @RequestBody @Valid ProductRequest request) {
        return productService.updateProduct(id, request);
    }

    // GET /api/products
    @GetMapping(params = "!page")
    public ServiceResult<List<ProductResponse>> getAllProducts() {
        return productService.getAllProducts(); // -> 200 OK
    }

    // GET /api/products?page=1&limit=12&sort=latest&q=zapato
    // Cuando viene "page" en la query, entra acá.
    @GetMapping(params = "page")
    public ServiceResult<PaginatedResponse<ProductResponse>> getAllProductsPaged(@Valid ProductPaginatedRequest params) {
        return productService.getAllProductsPaged(params);
    }

    // GET /api/products/{id}
    @GetMapping("/{id}")
    public ServiceResult<ProductResponse> getProductById(@PathVariable Long id) {
        return productService.getProductById(id); // -> 200 OK o 404 vía excepción/advice
    }

    @GetMapping("/{id}/details")
    public ServiceResult<ProductDetailsResponse> getProductDetails(@PathVariable Long id) {
        return productDetailsService.getDetails(id); // -> 200 OK (ServiceResult.ok)
    }

    @Operation(summary = "Facetas de productos (categorías, marcas y rango de precio)")
    @GetMapping("/facets")
    public ServiceResult<ProductFacetsResponse> getFacets(ProductPaginatedRequest params) {
        return productService.getProductFacets(params);
    }
}
