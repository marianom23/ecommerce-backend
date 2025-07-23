package com.empresa.ecommerce_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "admin_users",
        indexes = @Index(name = "idx_admin_users_username", columnList = "username", unique = true)
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    @NotBlank
    @ToString.Include
    private String username;

    @Column(nullable = false)
    @NotBlank
    private String password;          // sin @Exclude

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "adminuser_roles",
            joinColumns = @JoinColumn(name = "adminuser_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();  // sin @Exclude
}
