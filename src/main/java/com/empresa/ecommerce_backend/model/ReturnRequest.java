package com.empresa.ecommerce_backend.model;

import com.empresa.ecommerce_backend.enums.ReturnRequestStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "return_requests",
        indexes = {
                @Index(name = "idx_rr_order", columnList = "order_id"),
                @Index(name = "idx_rr_product", columnList = "product_id"),
                @Index(name = "idx_rr_user", columnList = "user_id"),
                @Index(name = "idx_rr_status", columnList = "status"),
                @Index(name = "idx_rr_requested_at", columnList = "requestedAt")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"order", "product", "user"})
public class ReturnRequest {

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @Column(nullable = false, length = 500)
    @NotBlank
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull
    private ReturnRequestStatus status;

    @Column(nullable = false)
    @NotNull
    private LocalDateTime requestedAt;

    private LocalDateTime resolvedAt;

    @PrePersist
    private void prePersist() {
        if (requestedAt == null) requestedAt = LocalDateTime.now();
        if (status == null) status = ReturnRequestStatus.PENDING;
    }
}
