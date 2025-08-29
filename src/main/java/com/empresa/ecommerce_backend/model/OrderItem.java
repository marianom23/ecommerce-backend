package com.empresa.ecommerce_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "order_items",
        indexes = {
                @Index(name = "idx_order_items_order", columnList = "order_id"),
                @Index(name = "idx_order_items_variant", columnList = "variant_id")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_order_variant",
                columnNames = {"order_id", "variant_id"}
        )
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"order", "variant"})
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull
    private Order order;

    // 👉 ahora la relación es con la variante
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    @NotNull
    private ProductVariant variant;

    // Snapshots útiles para reporting/render:
    @Column(length = 200, nullable = false)
    @NotNull
    private String productName;   // name del Product padre

    @Column(length = 100)
    private String sku;           // sku de la variante

    @Column(length = 1000)
    private String attributesJson; // {"size":"M","color":"Red"}

    @Column(nullable = false, precision = 15, scale = 2)
    @NotNull @Min(0)
    private BigDecimal unitPrice; // 👉 precio snapshot de la variante

    @Column(nullable = false)
    @NotNull @Min(1)
    private Integer quantity;

    @Column(precision = 15, scale = 2)
    @Min(0)
    private BigDecimal discountAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    @NotNull @Min(0)
    private BigDecimal lineTotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_lot_id")
    private PurchaseLot purchaseLot; // si usás lotes de compra
}
