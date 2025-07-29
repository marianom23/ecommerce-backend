package com.empresa.ecommerce_backend.model;

import com.empresa.ecommerce_backend.enums.InventoryMovementType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "inventory_movements",
        indexes = {
                @Index(name = "idx_inv_mov_product", columnList = "product_id"),
                @Index(name = "idx_inv_mov_user", columnList = "user_id"),
                @Index(name = "idx_inv_mov_date", columnList = "movementDate")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"product", "user"})
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull
    private Product product;

    /** Positivo: ingreso | Negativo: egreso */
    @Column(nullable = false)
    @NotNull
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @NotNull
    private InventoryMovementType type;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @NotNull
    private LocalDateTime movementDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // opcional

}
