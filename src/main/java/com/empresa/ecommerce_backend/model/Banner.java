// src/main/java/com/empresa/ecommerce_backend/model/Banner.java
package com.empresa.ecommerce_backend.model;

import com.empresa.ecommerce_backend.enums.BannerPlacement;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "banners")
@Getter
@Setter
@NoArgsConstructor
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BannerPlacement placement;

    @Column(length = 255)
    private String title;

    @Column(length = 255)
    private String subtitle;

    @Column(length = 1000)
    private String description;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 100)
    private String ctaText;

    @Column(length = 500)
    private String ctaUrl;

    // Precios / descuento (opcionales)
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal oldPrice;

    private Integer discountPercent;

    // Para banners con cuenta regresiva (opcional)
    private LocalDateTime countdownUntil;

    // Orden dentro del placement
    private Integer sortOrder;

    @Column(nullable = false)
    private boolean active = true;
}
