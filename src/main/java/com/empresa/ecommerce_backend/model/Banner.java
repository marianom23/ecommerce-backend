// src/main/java/com/empresa/ecommerce_backend/model/Banner.java
package com.empresa.ecommerce_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "banners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 200)
    @NotBlank
    private String title;

    @Column(nullable = false, length = 500)
    @NotBlank
    private String imageUrl;

    @Column(length = 500)
    private String linkUrl;

    @Column(nullable = false)
    private Boolean active;

    private Integer position; // orden en el slider

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    @PrePersist
    private void prePersist() {
        if (active == null) active = true;
        if (startAt == null) startAt = LocalDateTime.now();
    }
}
