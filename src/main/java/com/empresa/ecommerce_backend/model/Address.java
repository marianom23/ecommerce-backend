package com.empresa.ecommerce_backend.model;

import com.empresa.ecommerce_backend.enums.AddressType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(
        name = "addresses",
        indexes = {
                @Index(name = "idx_addresses_user", columnList = "user_id"),
                @Index(name = "idx_addresses_type", columnList = "type")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"user"})
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 150)
    @NotBlank
    private String street;

    @Column(length = 20)
    private String streetNumber;

    @Column(nullable = false, length = 100)
    @NotBlank
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 20)
    private String postalCode;

    @Column(nullable = false, length = 100)
    @NotBlank
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @NotNull
    private AddressType type;

    @Column(length = 20)
    private String apartmentNumber;

    @Column(length = 10)
    private String floor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
