package com.empresa.ecommerce_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "purchase_orders",
        indexes = {
                @Index(name = "idx_purchase_orders_supplier", columnList = "supplier"),
                @Index(name = "idx_purchase_orders_date", columnList = "purchaseDate")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "lots")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(length = 200, nullable = false)
    @NotNull
    private String supplier;

    @Column(nullable = false)
    @NotNull
    private LocalDate purchaseDate;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<PurchaseLot> lots = new HashSet<>();

    @PrePersist
    private void prePersist() {
        if (purchaseDate == null) {
            purchaseDate = LocalDate.now();
        }
    }
}
