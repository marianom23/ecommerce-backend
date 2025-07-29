package com.empresa.ecommerce_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "purchase_lots",
        indexes = {
                @Index(name = "idx_purchase_lots_product", columnList = "product_id"),
                @Index(name = "idx_purchase_lots_order", columnList = "purchase_order_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"purchaseOrder", "product"})
public class PurchaseLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;

    @Column(nullable = false)
    @NotNull
    @Min(1)
    private Integer quantity;

    @Column(nullable = false, precision = 15, scale = 2)
    @NotNull
    @Min(0)
    private BigDecimal unitCost; // precio sin impuestos

    @Column(precision = 5, scale = 2)
    @Min(0)
    private BigDecimal taxPercentage;

    @Column(length = 500)
    private String notes; // opcional, observaciones del lote

}
