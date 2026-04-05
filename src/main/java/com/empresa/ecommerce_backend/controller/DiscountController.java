package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.model.Discount;
import com.empresa.ecommerce_backend.repository.DiscountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountRepository repository;

    @GetMapping
    public ResponseEntity<List<Discount>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ServiceResult<Discount>> create(@RequestBody Discount discount) {
        if (repository.existsByName(discount.getName())) {
            return ResponseEntity.badRequest().body(ServiceResult.error(HttpStatus.BAD_REQUEST, "Ya existe un descuento con ese nombre"));
        }
        Discount saved = repository.save(discount);
        return ResponseEntity.status(HttpStatus.CREATED).body(ServiceResult.created(saved));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ServiceResult<Discount>> update(@PathVariable Long id, @RequestBody Discount discount) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setName(discount.getName());
                    existing.setPercentage(discount.getPercentage());
                    existing.setAmount(discount.getAmount());
                    existing.setStartDate(discount.getStartDate());
                    existing.setEndDate(discount.getEndDate());
                    existing.setGlobal(discount.isGlobal());
                    existing.setProductType(discount.getProductType());
                    repository.save(existing);
                    return ResponseEntity.ok(ServiceResult.ok(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
