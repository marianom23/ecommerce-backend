package com.empresa.ecommerce_backend.model;

import com.empresa.ecommerce_backend.enums.AttributeScope;
import com.empresa.ecommerce_backend.enums.ProductType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "attribute_templates")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AttributeTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttributeScope scope;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", length = 30)
    private ProductType productType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AttributeValue> values = new ArrayList<>();

    public void addValue(AttributeValue value) {
        values.add(value);
        value.setTemplate(this);
    }
}
