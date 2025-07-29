package com.empresa.ecommerce_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "discounts",
        indexes = {
                @Index(name = "idx_discounts_name", columnList = "name", unique = true),
                @Index(name = "idx_discounts_start_end", columnList = "startDate,endDate")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "products")
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true, length = 200)
    @NotBlank
    private String name;

    @Column(precision = 5, scale = 2)
    @DecimalMin("0.00")
    private BigDecimal percentage;

    @Column(precision = 15, scale = 2)
    @DecimalMin("0.00")
    private BigDecimal amount;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @ManyToMany(mappedBy = "discounts", fetch = FetchType.LAZY)
    private Set<Product> products = new HashSet<>();

}
