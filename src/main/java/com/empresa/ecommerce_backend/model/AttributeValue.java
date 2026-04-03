package com.empresa.ecommerce_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attribute_values")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AttributeValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private AttributeTemplate template;

    @Column(nullable = false, length = 100)
    private String label; // Nombre amigable (ej: "Global")

    @Column(nullable = false, length = 100)
    private String value; // Valor técnico (ej: "GLOBAL")
}
