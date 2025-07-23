package com.empresa.ecommerce_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "brands",
        indexes = @Index(name = "idx_brands_name", columnList = "name", unique = true)
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"products"})
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    @NotBlank
    private String name;

    @Column(length = 500)
    private String logoUrl;

    @OneToMany(mappedBy = "brand", fetch = FetchType.LAZY)
    private Set<Product> products = new HashSet<>();
}
