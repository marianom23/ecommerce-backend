// src/main/java/com/empresa/ecommerce_backend/model/User.java
package com.empresa.ecommerce_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email", unique = true),
                @Index(name = "idx_users_last_name", columnList = "lastName")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    @NotBlank
    @ToString.Include
    private String firstName;

    @Column(nullable = false)
    @NotBlank
    @ToString.Include
    private String lastName;

    @Column(nullable = false, unique = true)
    @NotBlank
    @Email
    @ToString.Include
    private String email;

    @Column(nullable = false)
    @NotBlank
    private String password;

    @Column(nullable = false)
    private boolean verified = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<Address> addresses = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Cart cart;

    @OneToMany(mappedBy = "user")
    private Set<Order> orders = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Review> reviews = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Wishlist> wishlists = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Notification> notifications = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<SupportTicket> supportTickets = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<ReturnRequest> returnRequests = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<ProductView> productViews = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<InventoryMovement> inventoryMovements = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<AuditLog> auditLogs = new HashSet<>();

    @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<PaymentCard> paymentCards = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "user_coupons",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "coupon_id")
    )
    private Set<Coupon> coupons = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<LoginAttempt> loginAttempts = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<BankTransfer> bankTransfers = new HashSet<>();
}
