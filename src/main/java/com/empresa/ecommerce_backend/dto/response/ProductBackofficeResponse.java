package com.empresa.ecommerce_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductBackofficeResponse {
    private Long id;
    private String name;
    private String thumbnail;           // Primera imagen del producto o variante
    private String categoryName;        // Nombre de la categoría
    private String brandName;           // Nombre de la marca
    private BigDecimal price;           // Precio representativo (más barato)
    private Integer totalStock;         // Stock total de todas las variantes
    private Integer variantCount;       // Cantidad de variantes
}
