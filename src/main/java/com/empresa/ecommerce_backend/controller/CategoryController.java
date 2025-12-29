package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.request.CategoryRequest;
import com.empresa.ecommerce_backend.dto.response.CategoryResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.exception.RecursoNoEncontradoException;
import com.empresa.ecommerce_backend.model.Category;
import com.empresa.ecommerce_backend.repository.CategoryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public ServiceResult<List<CategoryResponse>> listAll() {
        List<CategoryResponse> categories = categoryRepository.findAll()
                .stream()
                .map(c -> new CategoryResponse(
                        c.getId(), 
                        c.getName(), 
                        c.getImageUrl(),
                        c.getParent() != null ? c.getParent().getId() : null
                ))
                .collect(Collectors.toList());
        
        return ServiceResult.ok(categories);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ServiceResult<CategoryResponse> getById(@PathVariable Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
        
        return ServiceResult.ok(new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getImageUrl(),
                category.getParent() != null ? category.getParent().getId() : null
        ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ServiceResult<CategoryResponse> create(@RequestBody @Valid CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setImageUrl(request.getImageUrl());
        
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoría padre no encontrada"));
            category.setParent(parent);
        }
        
        Category saved = categoryRepository.save(category);
        
        return ServiceResult.created(new CategoryResponse(
                saved.getId(),
                saved.getName(),
                saved.getImageUrl(),
                saved.getParent() != null ? saved.getParent().getId() : null
        ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ServiceResult<CategoryResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid CategoryRequest request) {
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
        
        category.setName(request.getName());
        category.setImageUrl(request.getImageUrl());
        
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Categoría padre no encontrada"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
        
        Category saved = categoryRepository.save(category);
        
        return ServiceResult.ok(new CategoryResponse(
                saved.getId(),
                saved.getName(),
                saved.getImageUrl(),
                saved.getParent() != null ? saved.getParent().getId() : null
        ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ServiceResult<Void> delete(@PathVariable Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Categoría no encontrada");
        }
        categoryRepository.deleteById(id);
        return ServiceResult.ok(null);
    }
}
