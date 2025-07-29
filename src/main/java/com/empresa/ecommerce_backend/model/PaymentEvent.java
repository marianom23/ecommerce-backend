package com.empresa.ecommerce_backend.model;

import com.empresa.ecommerce_backend.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "payment_events",
        indexes = {
                @Index(name = "idx_payment_events_payment", columnList = "payment_id"),
                @Index(name = "idx_payment_events_when", columnList = "eventAt")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "payment")
public class PaymentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    @NotNull
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PaymentStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PaymentStatus toStatus;

    @Column(nullable = false)
    @NotNull
    private LocalDateTime eventAt;

    @Column(length = 100)
    private String triggeredBy;

    @Column(length = 500)
    private String note;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String payload;

}
