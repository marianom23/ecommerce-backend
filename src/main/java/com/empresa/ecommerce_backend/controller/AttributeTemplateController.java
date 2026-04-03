package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.request.AttributeTemplateRequest;
import com.empresa.ecommerce_backend.dto.response.AttributeTemplateResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.enums.AttributeScope;
import com.empresa.ecommerce_backend.enums.ProductType;
import com.empresa.ecommerce_backend.service.interfaces.AttributeTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attribute-templates")
@RequiredArgsConstructor
public class AttributeTemplateController {

    private final AttributeTemplateService service;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ServiceResult<AttributeTemplateResponse> create(@RequestBody @Valid AttributeTemplateRequest request) {
        return service.createTemplate(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ServiceResult<AttributeTemplateResponse> update(@PathVariable Long id, @RequestBody @Valid AttributeTemplateRequest request) {
        return service.updateTemplate(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ServiceResult<Void> delete(@PathVariable Long id) {
        return service.deleteTemplate(id);
    }

    @GetMapping
    public ServiceResult<List<AttributeTemplateResponse>> getAll() {
        return service.getAllTemplates();
    }

    @GetMapping("/applicable")
    public ServiceResult<List<AttributeTemplateResponse>> getApplicable(
            @RequestParam AttributeScope scope,
            @RequestParam(required = false) ProductType type,
            @RequestParam(required = false) Long categoryId) {
        return service.getApplicableTemplates(scope, type, categoryId);
    }
}
