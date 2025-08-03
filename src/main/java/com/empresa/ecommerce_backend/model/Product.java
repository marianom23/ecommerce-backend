package com.empresa.ecommerce_backend.model;

import com.empresa.ecommerce_backend.enums.StockTrackingMode;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_products_name", columnList = "name"),
                @Index(name = "idx_products_sku", columnList = "sku", unique = true),
                @Index(name = "idx_products_category", columnList = "category_id"),
                @Index(name = "idx_products_brand", columnList = "brand_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"discounts", "tags"})
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

    @Column(nullable = false, precision = 15, scale = 2)
    @NotNull
    @Min(0)
    private BigDecimal price;

    @Column(nullable = false)
    @NotNull
    @Min(0)
    private Integer stock = 0;

    @Column(length = 100, unique = true)
    private String sku;

    @Column(length = 500)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // ------ NUEVOS CAMPOS ------
    @Column(precision = 10, scale = 3)
    private BigDecimal weight;    // en kilogramos (ajusta si prefieres gramos)

    @Column(precision = 10, scale = 2)
    private BigDecimal length;    // en cm

    @Column(precision = 10, scale = 2)
    private BigDecimal width;     // en cm

    @Column(precision = 10, scale = 2)
    private BigDecimal height;    // en cm

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private StockTrackingMode stockTrackingMode = StockTrackingMode.SIMPLE;

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
}
