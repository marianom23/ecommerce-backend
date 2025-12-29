package com.empresa.ecommerce_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAdminResponse {
    private Long id;
    private String name;
    private String description;
    private String sku;
    private Long brandId;
    private Long categoryId;
    private Long soldCount;
}
