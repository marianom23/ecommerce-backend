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
                @Index(name = "idx_order_items_product", columnList = "product_id")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_order_product",
                columnNames = {"order_id", "product_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"order", "product"})
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull
    private Product product;

    @Column(length = 200, nullable = false)
    @NotNull
    private String productName;

    @Column(length = 100)
    private String sku;

    @Column(nullable = false, precision = 15, scale = 2)
    @NotNull
    @Min(0)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    @NotNull
    @Min(1)
    private Integer quantity;

    @Column(precision = 15, scale = 2)
    @Min(0)
    private BigDecimal discountAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    @NotNull
    @Min(0)
    private BigDecimal lineTotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_lot_id")
    private PurchaseLot purchaseLot; // Lote desde el cual se vendió este producto

}
