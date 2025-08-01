package com.empresa.ecommerce_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "carts",
        indexes = {
                @Index(name = "idx_carts_user", columnList = "user_id", unique = false),
                @Index(name = "idx_carts_session", columnList = "session_id", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"user", "items"})
public class Cart {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1) Relación opcional con usuario
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true, unique = true)
    private User user;

    // 2) Identificador de carrito anónimo
    @Column(name = "session_id", length = 100, unique = true, nullable = true)
    private String sessionId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CartItem> items = new HashSet<>();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

}
