package com.empresa.ecommerce_backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    @Min(0)
    private BigDecimal price;

    @NotNull
    @Min(0)
    private Integer stock;

    private String sku;

    private String imageUrl;

    private Long brandId;

    private Long categoryId;

    // Nuevo: Peso del producto en kilogramos (ajusta según la unidad que manejes)
    @DecimalMin(value = "0.0", inclusive = false, message = "El peso debe ser mayor a 0")
    private BigDecimal weight;

    // Nuevos: Dimensiones (en cm, por ejemplo)
    @DecimalMin(value = "0.0", inclusive = false, message = "El largo debe ser mayor a 0")
    private BigDecimal length;

    @DecimalMin(value = "0.0", inclusive = false, message = "El ancho debe ser mayor a 0")
    private BigDecimal width;

    @DecimalMin(value = "0.0", inclusive = false, message = "La altura debe ser mayor a 0")
    private BigDecimal height;
}
