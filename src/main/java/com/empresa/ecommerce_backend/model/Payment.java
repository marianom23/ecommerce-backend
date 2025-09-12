// src/main/java/com/empresa/ecommerce_backend/model/Payment.java
package com.empresa.ecommerce_backend.model;

import com.empresa.ecommerce_backend.enums.PaymentMethod;
import com.empresa.ecommerce_backend.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payments_order", columnList = "order_id", unique = true),
                @Index(name = "idx_payments_status", columnList = "status"),
                @Index(name = "idx_payments_method", columnList = "method"),
                // payment_id de MP (único cuando llega)
                @Index(name = "idx_payments_provider_payment", columnList = "providerPaymentId", unique = true),
                // preference_id de MP (NO único)
                @Index(name = "idx_payments_provider_pref", columnList = "providerPreferenceId")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "order")
public class Payment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    @NotNull
    private Order order;

    @Column(nullable = false, precision = 15, scale = 2) @NotNull @DecimalMin("0.00")
    private BigDecimal amount;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) @NotNull
    private PaymentMethod method;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) @NotNull
    private PaymentStatus status;

    @Column(length = 100)
    private String provider; // "MERCADO_PAGO"

    // Nuevo: preference_id (no único)
    @Column(length = 150)
    private String providerPreferenceId;

    // payment_id real de MP (único cuando existe)
    @Column(length = 150, unique = true)
    private String providerPaymentId;

    @Column
    private LocalDateTime expiresAt;

    @Column(length = 200)
    private String transferReference;

    @Column(length = 300)
    private String receiptUrl;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String providerMetadata; // JSON (init_point, etc.)

    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name="updated_at")
    private LocalDateTime updatedAt;
}
