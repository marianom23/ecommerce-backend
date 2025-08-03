package com.empresa.ecommerce_backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantRequest {
    @NotBlank
    private String sku;
    @NotNull
    @Min(0) private BigDecimal price;

    private String attributesJson; // {"size":"M","color":"Red"}
}
