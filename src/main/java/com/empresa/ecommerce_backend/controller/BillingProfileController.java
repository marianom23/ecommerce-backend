// src/main/java/com/empresa/ecommerce_backend/controller/BillingProfileController.java
package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.request.BillingProfileRequest;
import com.empresa.ecommerce_backend.dto.response.BillingProfileResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.service.interfaces.BillingProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/billing-profiles")
@RequiredArgsConstructor
public class BillingProfileController {

    private final BillingProfileService service;

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping
    public ServiceResult<List<BillingProfileResponse>> listMine() {
        return service.listForCurrentUser();
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public ServiceResult<BillingProfileResponse> create(@RequestBody @Valid BillingProfileRequest dto) {
        return service.createForCurrentUser(dto);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/{id}")
    public ServiceResult<BillingProfileResponse> update(@PathVariable Long id, @RequestBody @Valid BillingProfileRequest dto) {
        return service.updateForCurrentUser(id, dto);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/{id}")
    public ServiceResult<Void> delete(@PathVariable Long id) {
        return service.deleteForCurrentUser(id);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/{id}/default")
    public ServiceResult<BillingProfileResponse> setDefault(@PathVariable Long id) {
        return service.setDefault(id);
    }
}
