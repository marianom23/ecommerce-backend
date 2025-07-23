package com.empresa.ecommerce_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(
        name = "payment_cards",
        indexes = {
                @Index(name = "idx_payment_cards_user", columnList = "user_id"),
                @Index(name = "idx_payment_cards_token", columnList = "gatewayToken", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "user")
public class PaymentCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 50)
    @NotBlank
    private String brand; // VISA, MASTERCARD, etc.

    @Column(nullable = false, length = 4)
    @NotBlank
    @Size(min = 4, max = 4)
    @Pattern(regexp = "\\d{4}")
    private String lastFourDigits;

    @Column(nullable = false, length = 2)
    @NotBlank
    @Size(min = 2, max = 2)
    @Pattern(regexp = "0[1-9]|1[0-2]")
    private String expirationMonth;

    @Column(nullable = false, length = 4)
    @NotBlank
    @Size(min = 4, max = 4)
    @Pattern(regexp = "\\d{4}")
    private String expirationYear;

    @Column(nullable = false, unique = true, length = 255)
    @NotBlank
    private String gatewayToken;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    private void prePersist() {
        // En caso de que quieras normalizar datos:
        if (brand != null) brand = brand.trim().toUpperCase();
    }
}
