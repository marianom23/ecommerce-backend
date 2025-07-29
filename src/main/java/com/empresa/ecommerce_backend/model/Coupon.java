package com.empresa.ecommerce_backend.model;

import com.empresa.ecommerce_backend.enums.CouponType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "coupons",
        indexes = @Index(name = "idx_coupons_code", columnList = "code", unique = true)
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "users")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    @NotBlank
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull
    private CouponType type;

    @Column(precision = 15, scale = 2)
    private BigDecimal value; // % o monto según type

    private Integer usageLimit;    // total
    private Integer usagePerUser;  // por usuario
    private Integer usedCount;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    @Column(nullable = false)
    private Boolean active;

    @ManyToMany(mappedBy = "coupons", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();

}
