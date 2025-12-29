package com.empresa.ecommerce_backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ProductVariantRequest {

    @NotBlank
    private String sku;                   // único por variante

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal price;

    @Min(0)
    private Integer stock;                // Opcional para digitales

    private com.empresa.ecommerce_backend.enums.FulfillmentType fulfillmentType;

    // Logística (Andreani) - Opcionales (validado en Service si es PHYSICAL)
    private BigDecimal weightKg;
    private BigDecimal lengthCm;
    private BigDecimal widthCm;
    private BigDecimal heightCm;

    // Atributos (talle/color/etc.) si no los modelás aún
    private String attributesJson; // {"size":"M","color":"Red"}
}
