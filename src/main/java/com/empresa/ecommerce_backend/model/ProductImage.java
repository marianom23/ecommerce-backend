package com.empresa.ecommerce_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(
        name = "product_images",
        indexes = @Index(name = "idx_product_images_product", columnList = "product_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "product")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id") // null = imagen general del producto
    private ProductVariant variant;


    @Column(nullable = false, length = 500)
    @NotBlank
    private String url;

    @Column(length = 255)
    private String altText;

    private Integer position;
}
