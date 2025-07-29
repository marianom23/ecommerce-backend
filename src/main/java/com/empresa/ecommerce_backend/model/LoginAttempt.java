package com.empresa.ecommerce_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "login_attempts",
        indexes = {
                @Index(name = "idx_login_attempts_user", columnList = "user_id"),
                @Index(name = "idx_login_attempts_attemptAt", columnList = "attemptAt")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "user")
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // puede ser null si el email/usuario no existe

    @Column(nullable = false)
    @NotNull
    private LocalDateTime attemptAt;

    @Column(length = 45) // IPv6 compatible
    private String ipAddress;

    @Column(nullable = false)
    @NotNull
    private Boolean success;

    @Column(length = 255)
    private String reason; // "Wrong password", "User not found", ...

}
