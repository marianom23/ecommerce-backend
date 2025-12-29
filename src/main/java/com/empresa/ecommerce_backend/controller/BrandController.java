package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.request.BrandRequest;
import com.empresa.ecommerce_backend.dto.response.BrandResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.exception.RecursoNoEncontradoException;
import com.empresa.ecommerce_backend.model.Brand;
import com.empresa.ecommerce_backend.repository.BrandRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandRepository brandRepository;

    @GetMapping
    public ServiceResult<List<BrandResponse>> listAll() {
        List<BrandResponse> brands = brandRepository.findAll()
                .stream()
                .map(b -> new BrandResponse(b.getId(), b.getName(), b.getLogoUrl()))
                .collect(Collectors.toList());
        
        return ServiceResult.ok(brands);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ServiceResult<BrandResponse> getById(@PathVariable Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Marca no encontrada"));
        
        return ServiceResult.ok(new BrandResponse(
                brand.getId(),
                brand.getName(),
                brand.getLogoUrl()
        ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ServiceResult<BrandResponse> create(@RequestBody @Valid BrandRequest request) {
        Brand brand = new Brand();
        brand.setName(request.getName());
        brand.setLogoUrl(request.getLogoUrl());
        
        Brand saved = brandRepository.save(brand);
        
        return ServiceResult.created(new BrandResponse(
                saved.getId(),
                saved.getName(),
                saved.getLogoUrl()
        ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ServiceResult<BrandResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid BrandRequest request) {
        
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Marca no encontrada"));
        
        brand.setName(request.getName());
        brand.setLogoUrl(request.getLogoUrl());
        
        Brand saved = brandRepository.save(brand);
        
        return ServiceResult.ok(new BrandResponse(
                saved.getId(),
                saved.getName(),
                saved.getLogoUrl()
        ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ServiceResult<Void> delete(@PathVariable Long id) {
        if (!brandRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Marca no encontrada");
        }
        brandRepository.deleteById(id);
        return ServiceResult.ok(null);
    }
}
