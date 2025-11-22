// src/main/java/com/empresa/ecommerce_backend/controller/BannerController.java
package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.request.BannerRequest;
import com.empresa.ecommerce_backend.dto.response.BannerResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.enums.BannerPlacement;
import com.empresa.ecommerce_backend.service.interfaces.BannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    // Público: el front los consume para mostrar home
    @GetMapping
    public ServiceResult<List<BannerResponse>> list(
            @RequestParam(required = false) BannerPlacement placement
    ) {
        return bannerService.getActiveBanners(placement);
    }

    // Solo admin puede crear/editar banners
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ServiceResult<BannerResponse> create(@RequestBody @Valid BannerRequest request) {
        return bannerService.create(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ServiceResult<BannerResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid BannerRequest request
    ) {
        return bannerService.update(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ServiceResult<Void> delete(@PathVariable Long id) {
        return bannerService.delete(id);
    }
}
