// src/main/java/com/empresa/ecommerce_backend/model/BillingProfile.java
package com.empresa.ecommerce_backend.model;

import com.empresa.ecommerce_backend.enums.DocumentType;
import com.empresa.ecommerce_backend.enums.TaxCondition;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "billing_profiles",
        indexes = {
                @Index(name = "idx_bp_user", columnList = "user_id"),
                @Index(name = "idx_bp_default", columnList = "is_default")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"user"})
public class BillingProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull
    private DocumentType documentType;   // DNI / CUIT / CUIL

    @Column(nullable = false, length = 20)
    @NotBlank
    private String documentNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @NotNull
    private TaxCondition taxCondition;   // CONSUMIDOR_FINAL / MONOTRIBUTO / etc.

    @Column(length = 150)
    private String businessName;         // Razón Social (si corresponde)

    @Email
    @Column(length = 150)
    private String emailForInvoices;     // Email para envío de factura

    @Column(length = 50)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "billing_address_id", nullable = false)
    @NotNull
    private Address billingAddress;      // Debe ser type=BILLING

    @Column(name = "is_default", nullable = false)
    private boolean defaultProfile = false; // ✅ NO usar "isDefault" como nombre de campo

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
