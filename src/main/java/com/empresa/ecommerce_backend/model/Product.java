package com.empresa.ecommerce_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import com.empresa.ecommerce_backend.enums.ProductType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_products_name", columnList = "name"),
                @Index(name = "idx_products_category", columnList = "category_id"),
                @Index(name = "idx_products_brand", columnList = "brand_id"),
                @Index(name = "idx_products_console", columnList = "console_id"),
                @Index(name = "idx_products_type", columnList = "product_type")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"discounts", "tags", "variants", "images", "dlcs"})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 200)
    @NotBlank
    private String name;

    @Column(length = 2000)
    private String description;

    // SKU BASE (opcional, ya no único, el SKU vendible está en la variante)
    @Column(length = 100)
    private String sku;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", length = 30)
    private ProductType productType;

    // Relaciones
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private Set<ProductImage> images = new HashSet<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductVariant> variants = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "console_id")
    private Console console;

    // Marketing
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_discount",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "discount_id")
    )
    private Set<Discount> discounts = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_tag",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @Column(name = "sold_count", nullable = false)
    private long soldCount = 0L;

    // --- Relación DLC: un DLC puede pertenecer a un juego padre (opcional) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_game_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Product parentGame;

    /** DLCs que pertenecen a este juego (relación inversa) */
    @OneToMany(mappedBy = "parentGame", fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Product> dlcs = new ArrayList<>();

    /** Especificaciones técnicas fijas del producto en formato JSON */
    @Column(name = "specifications_json", length = 2000)
    private String specificationsJson;

    /** Soporte para Preventas (Pre-orders) */
    @Column(name = "is_presale")
    private Boolean isPresale = false;

    @Column(name = "release_date")
    private LocalDateTime releaseDate;
}
