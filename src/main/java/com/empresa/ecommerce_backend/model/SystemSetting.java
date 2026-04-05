package com.empresa.ecommerce_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(
        name = "system_settings",
        indexes = @Index(name = "idx_system_settings_key", columnList = "config_key", unique = true)
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class SystemSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    @NotBlank
    private String key;

    @Column(name = "config_value", length = 1000)
    private String value;

    @Column(length = 255)
    private String description;

    @Column(name = "value_type", length = 50)
    private String type; // "STRING", "NUMBER", "BOOLEAN"
}
