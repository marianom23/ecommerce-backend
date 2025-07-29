package com.empresa.ecommerce_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "product_cost_history",
        indexes = {
                @Index(name = "idx_cost_history_product", columnList = "product_id"),
                @Index(name = "idx_cost_history_effective_from", columnList = "effectiveFrom")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "product")
public class ProductCostHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull
    private Product product;

    @Column(nullable = false, precision = 15, scale = 2)
    @NotNull
    @Min(0)
    private BigDecimal cost; // Costo neto del producto

    @Column(precision = 5, scale = 2)
    @Min(0)
    private BigDecimal taxPercentage;

    @Column(nullable = false)
    @NotNull
    private LocalDateTime effectiveFrom; // Desde cuándo aplica este costo

    public BigDecimal getCostWithTax() {
        BigDecimal tax = (taxPercentage == null) ? BigDecimal.ZERO : taxPercentage;
        return cost.add(cost.multiply(tax).divide(BigDecimal.valueOf(100)));
    }

    @PrePersist
    private void setDefaultDate() {
        if (effectiveFrom == null) {
            effectiveFrom = LocalDateTime.now();
        }
    }
}
