package com.empresa.ecommerce_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "product_variants",
        indexes = {
                @Index(name = "idx_variants_product", columnList = "product_id"),
                @Index(name = "idx_variants_sku", columnList = "sku", unique = true)
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"product", "images"})
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Imágenes específicas de la variante (si las usás)
    @OneToMany(mappedBy = "variant", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private Set<ProductImage> images = new HashSet<>();

    // Identificación vendible
    @Column(length = 100, unique = true, nullable = false)
    private String sku;

    // Precio y stock
    @Column(precision = 15, scale = 2, nullable = false)
    @NotNull
    private BigDecimal price;

    @Column(nullable = false)
    @NotNull @Min(0)
    private Integer stock = 0;

    // Logística (por variante)
    @Column(precision = 10, scale = 3, nullable = false)
    private BigDecimal weightKg;   // kg

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal lengthCm;   // cm

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal widthCm;    // cm

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal heightCm;   // cm

    // Atributos (talle/color/etc.) si no los modelás en tablas
    @Column(length = 1000)
    private String attributesJson; // {"size":"M","color":"Red"}
}
