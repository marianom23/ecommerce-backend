// src/main/java/com/empresa/ecommerce_backend/model/ShippingMethod.java
package com.empresa.ecommerce_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "shipping_methods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ShippingMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 150)
    @NotBlank
    private String name;

    @Column(precision = 15, scale = 2, nullable = false)
    @NotNull
    @DecimalMin("0.00")
    private BigDecimal cost;

    private Integer estimatedDays;

    @Column(nullable = false)
    @NotNull
    private Boolean active = true;

}
