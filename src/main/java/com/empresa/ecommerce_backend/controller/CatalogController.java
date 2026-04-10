package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.service.interfaces.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/admin/catalog")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/facebook")
    public ResponseEntity<byte[]> downloadFacebookCatalog() {
        ServiceResult<String> result = catalogService.generateFacebookCatalogCsv();
        
        if (!result.isSuccess()) {
            return ResponseEntity.internalServerError().build();
        }

        byte[] csvBytes = result.getData().getBytes(StandardCharsets.UTF_8);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("facebook_catalog.csv")
                .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }
}
