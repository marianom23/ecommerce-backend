// src/main/java/com/empresa/ecommerce_backend/dto/request/BannerRequest.java
package com.empresa.ecommerce_backend.dto.request;

import com.empresa.ecommerce_backend.enums.BannerPlacement;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class BannerRequest {

    private BannerPlacement placement;
    private String title;
    private String subtitle;
    private String description;
    private String imageUrl;
    private String ctaText;
    private String ctaUrl;
    private BigDecimal price;
    private BigDecimal oldPrice;
    private Integer discountPercent;
    private LocalDateTime countdownUntil;
    private Integer sortOrder;
    private Boolean active; // puede venir null en update
}
