// src/main/java/com/empresa/ecommerce_backend/dto/response/BannerResponse.java
package com.empresa.ecommerce_backend.dto.response;

import com.empresa.ecommerce_backend.enums.BannerPlacement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class BannerResponse {

    private Long id;
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
    private boolean active;
}
