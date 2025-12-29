package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.response.BrandResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.model.Brand;
import com.empresa.ecommerce_backend.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
