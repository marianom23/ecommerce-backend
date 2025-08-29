package com.empresa.ecommerce_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductRequest {

    @NotBlank
    private String name;

    private String description;

    // SKU base opcional (no es el SKU vendible)
    private String sku;

    // Opcional: si cargás una imagen “hero” del producto
    private String imageUrl;

    private Long brandId;
    private Long categoryId;
}
