// src/main/java/com/empresa/ecommerce_backend/model/Order.java
package com.empresa.ecommerce_backend.model;

import com.empresa.ecommerce_backend.enums.OrderStatus;
import com.empresa.ecommerce_backend.enums.PaymentMethod;
import com.empresa.ecommerce_backend.model.embeddable.AddressSnapshot;
import com.empresa.ecommerce_backend.model.embeddable.BillingSnapshot;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_orders_number", columnList = "orderNumber", unique = true),
                @Index(name = "idx_orders_user", columnList = "user_id"),
                @Index(name = "idx_orders_date", columnList = "orderDate")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"user", "items", "shipments", "payment"})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Column(nullable = false)
    @NotNull
    private LocalDateTime orderDate;

    // Desglose de montos (recomendado)
    @Column(nullable = false, precision = 15, scale = 2)
    @NotNull
    private BigDecimal subTotal;       // suma de lineas sin impuestos

    @Column(nullable = false, precision = 15, scale = 2)
    @NotNull
    private BigDecimal shippingCost;   // costo de envío

    @Column(nullable = false, precision = 15, scale = 2)
    @NotNull
    private BigDecimal taxAmount;      // impuestos

    @Column(nullable = false, precision = 15, scale = 2)
    @NotNull
    private BigDecimal discountTotal;  // descuentos a nivel pedido

    @Column(nullable = false, precision = 15, scale = 2)
    @NotNull
    private BigDecimal totalAmount;    // total final (guardás el snapshot del total)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @NotNull
    private OrderStatus status;

    // Usuario
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Ítems
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    // Snapshots
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "shipping_street")),
            @AttributeOverride(name = "streetNumber", column = @Column(name = "shipping_street_number")),
            @AttributeOverride(name = "city", column = @Column(name = "shipping_city")),
            @AttributeOverride(name = "state", column = @Column(name = "shipping_state")),
            @AttributeOverride(name = "postalCode", column = @Column(name = "shipping_postal_code")),
            @AttributeOverride(name = "country", column = @Column(name = "shipping_country")),
            @AttributeOverride(name = "apartmentNumber", column = @Column(name = "shipping_apartment_number")),
            @AttributeOverride(name = "floor", column = @Column(name = "shipping_floor")),
            @AttributeOverride(name = "recipientName", column = @Column(name = "shipping_recipient_name")),
            @AttributeOverride(name = "phone", column = @Column(name = "shipping_phone"))
    })
    private AddressSnapshot shippingAddress;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "fullName", column = @Column(name = "billing_full_name")),
            @AttributeOverride(name = "taxId", column = @Column(name = "billing_tax_id")),
            @AttributeOverride(name = "email", column = @Column(name = "billing_email")),
            @AttributeOverride(name = "phone", column = @Column(name = "billing_phone")),
            @AttributeOverride(name = "street", column = @Column(name = "billing_street")),
            @AttributeOverride(name = "streetNumber", column = @Column(name = "billing_street_number")),
            @AttributeOverride(name = "city", column = @Column(name = "billing_city")),
            @AttributeOverride(name = "state", column = @Column(name = "billing_state")),
            @AttributeOverride(name = "postalCode", column = @Column(name = "billing_postal_code")),
            @AttributeOverride(name = "country", column = @Column(name = "billing_country")),
            @AttributeOverride(name = "apartmentNumber", column = @Column(name = "billing_apartment_number")),
            @AttributeOverride(name = "floor", column = @Column(name = "billing_floor"))
    })
    private BillingSnapshot billingInfo;

    // Pagos: tu Payment ya es dueño (@OneToOne con FK en payments)
    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY)
    private Payment payment;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "chosen_payment_method", length = 30)
    private PaymentMethod chosenPaymentMethod;

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    // Envíos
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = false)
    private Set<Shipment> shipments = new HashSet<>();



    // Auditoría simple
    @Column(nullable = false) private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.orderDate == null) this.orderDate = this.createdAt;
        if (this.orderNumber == null) this.orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
