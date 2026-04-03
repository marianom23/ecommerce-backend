package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.model.Console;
import com.empresa.ecommerce_backend.repository.ConsoleRepository;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.empresa.ecommerce_backend.dto.response.ConsoleResponse;
import com.empresa.ecommerce_backend.repository.ConsoleRepository;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/consoles")
@RequiredArgsConstructor
public class ConsoleController {

    private final ConsoleRepository consoleRepository;

    @GetMapping
    public ServiceResult<List<ConsoleResponse>> getAll() {
        List<ConsoleResponse> list = consoleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ServiceResult.ok(list);
    }

    @GetMapping("/{id}")
    public ServiceResult<ConsoleResponse> getById(@PathVariable Long id) {
        return consoleRepository.findById(id)
                .map(c -> ServiceResult.ok(mapToResponse(c)))
                .orElse(ServiceResult.error(org.springframework.http.HttpStatus.NOT_FOUND, "Console not found"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ServiceResult<ConsoleResponse> create(@RequestBody Console console) {
        Console saved = consoleRepository.save(console);
        return ServiceResult.ok(mapToResponse(saved));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ServiceResult<ConsoleResponse> update(@PathVariable Long id, @RequestBody Console console) {
        return consoleRepository.findById(id)
                .map(existing -> {
                    existing.setName(console.getName());
                    existing.setDescription(console.getDescription());
                    existing.setImageUrl(console.getImageUrl());
                    existing.setBrand(console.getBrand());
                    Console saved = consoleRepository.save(existing);
                    return ServiceResult.ok(mapToResponse(saved));
                })
                .orElse(ServiceResult.error(org.springframework.http.HttpStatus.NOT_FOUND, "Console not found"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ServiceResult<Void> delete(@PathVariable Long id) {
        if (!consoleRepository.existsById(id)) {
            return ServiceResult.error(org.springframework.http.HttpStatus.NOT_FOUND, "Console not found");
        }
        consoleRepository.deleteById(id);
        return ServiceResult.ok(null);
    }

    private ConsoleResponse mapToResponse(Console c) {
        ConsoleResponse res = new ConsoleResponse();
        res.setId(c.getId());
        res.setName(c.getName());
        res.setDescription(c.getDescription());
        res.setImageUrl(c.getImageUrl());
        if (c.getBrand() != null) {
            res.setBrand(new ConsoleResponse.BrandSummary(c.getBrand().getId(), c.getBrand().getName()));
        }
        return res;
    }
}
