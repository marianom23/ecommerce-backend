package com.empresa.ecommerce_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "cart_items",
        indexes = {
                @Index(name = "idx_cart_items_cart",    columnList = "cart_id"),
                @Index(name = "idx_cart_items_product", columnList = "product_id")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_cart_prod_variant",
                columnNames = {"cart_id", "product_id", "variant_id"}
        )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"cart", "product", "variant"})
public class CartItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Variante concreta (puede ser null si no aplica) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer quantity;

    @NotNull
    @Column(name = "price_at_addition",             precision = 15, scale = 2, nullable = false)
    private BigDecimal priceAtAddition;

    @NotNull
    @Column(name = "discounted_price_at_addition",  precision = 15, scale = 2, nullable = false)
    private BigDecimal discountedPriceAtAddition;
}
