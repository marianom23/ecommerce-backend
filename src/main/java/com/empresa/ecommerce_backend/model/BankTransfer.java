// src/main/java/com/empresa/ecommerce_backend/model/BankTransfer.java
package com.empresa.ecommerce_backend.model;

import com.empresa.ecommerce_backend.enums.BankTransferStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "bank_transfers",
        indexes = {
                @Index(name = "idx_bt_order", columnList = "order_id"),
                @Index(name = "idx_bt_user", columnList = "user_id"),
                @Index(name = "idx_bt_tx_number", columnList = "transaction_number", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"order", "user"})
public class BankTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @NotNull
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @Column(nullable = false, length = 120)
    @NotBlank
    private String bankName;

    @Column(nullable = false, length = 50)
    @NotBlank
    private String accountNumber;

    @Column(name = "transaction_number", nullable = false, unique = true, length = 100)
    @NotBlank
    private String transactionNumber;

    @Column(nullable = false, precision = 15, scale = 2)
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal amount;

    @Column(nullable = false)
    @NotNull
    private LocalDateTime transferDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull
    private BankTransferStatus status;

    @Column(length = 500)
    private String receiptUrl;

    @PrePersist
    private void prePersist() {
        if (transferDate == null) transferDate = LocalDateTime.now();
        if (status == null) status = BankTransferStatus.PENDING;
    }
}
