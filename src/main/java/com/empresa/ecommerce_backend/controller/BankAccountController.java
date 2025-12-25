package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.request.BankAccountRequest;
import com.empresa.ecommerce_backend.dto.response.BankAccountResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.service.interfaces.BankAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bank-accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService service;

    // Público: para mostrar en el checkout
    @GetMapping
    public ServiceResult<List<BankAccountResponse>> getActiveAccounts() {
        return service.getAllActive();
    }

    // Admin: listar todas (incluso inactivas)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ServiceResult<List<BankAccountResponse>> getAllAccounts() {
        return service.getAllAdmin();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ServiceResult<BankAccountResponse> create(@RequestBody @Valid BankAccountRequest request) {
        return service.create(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ServiceResult<BankAccountResponse> update(@PathVariable Long id, @RequestBody @Valid BankAccountRequest request) {
        return service.update(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/toggle")
    public ServiceResult<BankAccountResponse> toggle(@PathVariable Long id) {
        return service.toggleActive(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ServiceResult<Void> delete(@PathVariable Long id) {
        return service.delete(id);
    }
}
