package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.request.SystemSettingUpdate;
import com.empresa.ecommerce_backend.model.SystemSetting;
import com.empresa.ecommerce_backend.repository.SystemSettingRepository;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SystemSettingController {

    private final SystemSettingRepository repository;

    @GetMapping
    public ResponseEntity<List<SystemSetting>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @PutMapping("/{key}")
    public ResponseEntity<ServiceResult<SystemSetting>> update(@PathVariable String key, @RequestBody SystemSettingUpdate dto) {
        return repository.findByKey(key)
                .map(setting -> {
                    setting.setValue(dto.getValue());
                    repository.save(setting);
                    return ResponseEntity.ok(ServiceResult.ok(setting));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{key}")
    public ResponseEntity<ServiceResult<SystemSetting>> create(@PathVariable String key, @RequestBody SystemSettingUpdate dto) {
        if (repository.findByKey(key).isPresent()) {
            return ResponseEntity.badRequest().body(ServiceResult.error(HttpStatus.BAD_REQUEST, "Setting already exists"));
        }
        SystemSetting s = SystemSetting.builder()
                .key(key)
                .value(dto.getValue())
                .type("NUMBER")
                .build();
        repository.save(s);
        return ResponseEntity.ok(ServiceResult.created(s));
    }
}
