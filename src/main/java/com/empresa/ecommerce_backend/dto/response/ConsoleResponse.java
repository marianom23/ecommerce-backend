package com.empresa.ecommerce_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsoleResponse {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private BrandSummary brand;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BrandSummary {
        private Long id;
        private String name;
    }
}
