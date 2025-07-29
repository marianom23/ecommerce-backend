package com.empresa.ecommerce_backend.model;

import com.empresa.ecommerce_backend.enums.ChargebackStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "chargebacks",
        indexes = {
                @Index(name = "idx_chargebacks_payment", columnList = "payment_id"),
                @Index(name = "idx_chargebacks_status", columnList = "status"),
                @Index(name = "idx_chargebacks_created", columnList = "receivedAt"),
                @Index(name = "idx_chargebacks_provider_id", columnList = "providerCaseId", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "payment")
public class Chargeback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    @NotNull
    private Payment payment;

    @Column(nullable = false, precision = 15, scale = 2)
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull
    private ChargebackStatus status;

    @Column(length = 150, unique = true)
    private String providerCaseId;

    @Column(nullable = false)
    private LocalDateTime receivedAt;

    private LocalDateTime resolvedAt;

    @Column(length = 500)
    private String reason;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String evidence;

}
