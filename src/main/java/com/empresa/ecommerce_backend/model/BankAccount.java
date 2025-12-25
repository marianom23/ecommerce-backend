package com.empresa.ecommerce_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "bank_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    @NotBlank
    private String bankName; // e.g. "Banco Galicia"

    @Column(nullable = false)
    @NotBlank
    private String holderName; // e.g. "Hornero Tech S.A."

    @Column(length = 20)
    private String cuil;

    @Column(length = 50)
    private String accountType; // e.g. "Caja de Ahorro", "Cuenta Corriente"

    @Column(nullable = false, unique = true)
    @NotBlank
    private String cbu;

    @Column(unique = true)
    private String alias;

    @Column(nullable = false)
    @NotBlank
    private String accountNumber;

    @Column(nullable = false)
    private boolean active = true;
}
