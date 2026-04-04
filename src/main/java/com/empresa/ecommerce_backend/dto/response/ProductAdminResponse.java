package com.empresa.ecommerce_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private Long consoleId;
    private String productType;
    private Long soldCount;
    private List<ProductImageResponse> images;

    // Juego padre (solo aplica cuando productType = DLC)
    private Long parentGameId;
    private String parentGameName;

    private String specificationsJson;

    // IDs de los descuentos individuales asociados al producto
    private List<Long> discountIds;

    private Boolean isPresale;
    private java.time.LocalDateTime releaseDate;
}
