package com.empresa.ecommerce_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

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
    private Long consoleId;
    private String productType; // GAME, DLC, etc.

    // Opcional: si el producto es un DLC, se puede indicar el juego al que pertenece
    private Long parentGameId;

    // Especificaciones técnicas fijas del producto en formato JSON
    private String specificationsJson;

    // IDs de los descuentos individuales asociados al producto
    private List<Long> discountIds;
}
