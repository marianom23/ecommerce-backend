// src/main/java/com/empresa/ecommerce_backend/controller/ProductController.java
package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.request.ProductRequest;
import com.empresa.ecommerce_backend.dto.response.ProductResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.service.interfaces.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ServiceResult<ProductResponse> createProduct(@RequestBody @Valid ProductRequest dto) {
        return productService.createProduct(dto);                 // -> 201 Created (ServiceResult.created)
    }

    @GetMapping
    public ServiceResult<List<ProductResponse>> getAllProducts() {
        return productService.getAllProducts();                  // -> 200 OK
    }

    @GetMapping("/{id}")
    public ServiceResult<ProductResponse> getProductById(@PathVariable Long id) {
        return productService.getProductById(id);                // -> 200 OK o 404 vía excepción/advice
    }
}
