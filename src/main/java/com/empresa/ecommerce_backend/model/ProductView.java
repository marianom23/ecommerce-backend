package com.empresa.ecommerce_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "product_views",
        indexes = {
                @Index(name = "idx_pv_user", columnList = "user_id"),
                @Index(name = "idx_pv_product", columnList = "product_id"),
                @Index(name = "idx_pv_viewedAt", columnList = "viewedAt")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"user", "product"})
public class ProductView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // puede ser null (anónimo)

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull
    private Product product;

    @Column(length = 255)
    private String ipAddress;

    @Column(length = 255)
    private String userAgent;

    @Column(nullable = false)
    @NotNull
    private LocalDateTime viewedAt;

    @PrePersist
    private void prePersist() {
        if (viewedAt == null) viewedAt = LocalDateTime.now();
    }
}
