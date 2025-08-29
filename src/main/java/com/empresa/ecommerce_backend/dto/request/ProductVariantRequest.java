package com.empresa.ecommerce_backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ProductVariantRequest {

    @NotNull
    private Long productId;               // a qué producto pertenece

    @NotBlank
    private String sku;                   // único por variante

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal price;

    @NotNull
    @Min(0)
    private Integer stock;

    // Logística (Andreani)
    @NotNull @DecimalMin(value = "0.001")  // kg
    private BigDecimal weightKg;

    @NotNull @DecimalMin(value = "0.01")   // cm
    private BigDecimal lengthCm;

    @NotNull @DecimalMin(value = "0.01")   // cm
    private BigDecimal widthCm;

    @NotNull @DecimalMin(value = "0.01")   // cm
    private BigDecimal heightCm;

    // Atributos (talle/color/etc.) si no los modelás aún
    private String attributesJson; // {"size":"M","color":"Red"}
}
