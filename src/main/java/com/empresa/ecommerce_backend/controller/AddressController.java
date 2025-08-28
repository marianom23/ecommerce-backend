// AddressController.java
package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.request.AddressRequest;
import com.empresa.ecommerce_backend.dto.response.AddressResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.enums.AddressType;
import com.empresa.ecommerce_backend.model.Address;
import com.empresa.ecommerce_backend.service.interfaces.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    // Solo autenticado; si querés, agregá @PreAuthorize según tu política
    @GetMapping
    public ServiceResult<List<AddressResponse>> list(@RequestParam(required = false) AddressType type) {
        return addressService.listForCurrentUser(type);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public ServiceResult<AddressResponse> create(@RequestBody @Valid AddressRequest dto) {
        return addressService.createForCurrentUser(dto);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/{id}")
    public ServiceResult<AddressResponse> update(@PathVariable Long id, @RequestBody @Valid AddressRequest dto) {
        return addressService.updateForCurrentUser(id, dto);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/{id}")
    public ServiceResult<Void> delete(@PathVariable Long id) {
        return addressService.deleteForCurrentUser(id);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/{id}/use")
    public ServiceResult<AddressResponse> touchUse(@PathVariable Long id) {
        return addressService.touchUse(id);
    }
}
