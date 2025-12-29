package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.response.CategoryResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.model.Category;
import com.empresa.ecommerce_backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
