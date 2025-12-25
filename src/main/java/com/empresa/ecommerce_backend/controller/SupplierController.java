package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.request.SupplierRequest;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.dto.response.SupplierResponse;
import com.empresa.ecommerce_backend.service.interfaces.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    // Listar proveedores: ¿Público o solo Admin? 
    // Generalmente es interno, así que ADMIN o MANAGER.
    // Pero si el usuario no especificó, lo dejaré protegido para ADMIN/MANAGER por seguridad,
    // o público si fuera necesario para filtros (pero proveedores suele ser backoffice).
    // Voy a asumir ADMIN por defecto para todo, como en BannerController (aunque Banner list es público).
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ServiceResult<List<SupplierResponse>> getAll() {
        return supplierService.getAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ServiceResult<SupplierResponse> getById(@PathVariable Long id) {
        return supplierService.getById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ServiceResult<SupplierResponse> create(@RequestBody @Valid SupplierRequest request) {
        return supplierService.create(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ServiceResult<SupplierResponse> update(@PathVariable Long id, @RequestBody @Valid SupplierRequest request) {
        return supplierService.update(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ServiceResult<Void> delete(@PathVariable Long id) {
        return supplierService.delete(id);
    }
}
