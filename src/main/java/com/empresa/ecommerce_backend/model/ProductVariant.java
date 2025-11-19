package com.empresa.ecommerce_backend.model;

import com.empresa.ecommerce_backend.enums.FulfillmentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "product_variants",
        indexes = {
                @Index(name = "idx_variants_product", columnList = "product_id"),
                @Index(name = "idx_variants_sku", columnList = "sku", unique = true)
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"product", "images"})
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "max_per_order")
    private Integer maxPerOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @OneToMany(mappedBy = "variant", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private Set<ProductImage> images = new HashSet<>();

    @Column(length = 100, unique = true, nullable = false)
    private String sku;

    @Column(precision = 15, scale = 2, nullable = false)
    @NotNull
    private BigDecimal price;

    // Para PHYSICAL se usa; para DIGITAL_ON_DEMAND se ignora
    @Column(nullable = false)
    @NotNull
    private Integer stock = 0;

    // Nuevo: tipo de fulfillment
    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_type", nullable = false, length = 30)
    private FulfillmentType fulfillmentType = FulfillmentType.PHYSICAL;

    // Opcional: SLA de entrega digital (para mostrar al cliente)
    @Column(name = "lead_time_min_hours")
    private Integer leadTimeMinHours;   // ej: 1

    @Column(name = "lead_time_max_hours")
    private Integer leadTimeMaxHours;   // ej: 12

    // Logística: para digitales puede ser NULL
    @Column(precision = 10, scale = 3)
    private BigDecimal weightKg;   // kg

    @Column(precision = 10, scale = 2)
    private BigDecimal lengthCm;   // cm

    @Column(precision = 10, scale = 2)
    private BigDecimal widthCm;    // cm

    @Column(precision = 10, scale = 2)
    private BigDecimal heightCm;   // cm

    @Column(length = 1000)
    private String attributesJson;

    @Column(name = "sold_count", nullable = false)
    private long soldCount = 0L;

    public boolean isDigital() {
        return fulfillmentType == FulfillmentType.DIGITAL_ON_DEMAND
                || fulfillmentType == FulfillmentType.DIGITAL_INSTANT;
    }

    public boolean isDigitalInstant() {
        return fulfillmentType == FulfillmentType.DIGITAL_INSTANT;
    }

    public boolean isDigitalOnDemand() {
        return fulfillmentType == FulfillmentType.DIGITAL_ON_DEMAND;
    }

    public boolean isPhysical() {
        return fulfillmentType == FulfillmentType.PHYSICAL;
    }

}
