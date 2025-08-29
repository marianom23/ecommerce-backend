// src/main/java/com/empresa/ecommerce_backend/model/embeddable/BillingSnapshot.java
package com.empresa.ecommerce_backend.model.embeddable;

import com.empresa.ecommerce_backend.enums.DocumentType;
import com.empresa.ecommerce_backend.enums.TaxCondition;
import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class BillingSnapshot {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentType documentType;

    @Column(nullable = false, length = 20)
    private String documentNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TaxCondition taxCondition;

    @Column(length = 150)
    private String businessName;

    @Column(length = 150)
    private String emailForInvoices;

    @Column(length = 50)
    private String phone;

    // Dirección fiscal “congelada”
    @Column(length = 150, nullable = false) private String street;
    @Column(length = 20)  private String streetNumber;
    @Column(length = 100, nullable = false) private String city;
    @Column(length = 100) private String state;
    @Column(length = 20)  private String postalCode;
    @Column(length = 100, nullable = false) private String country;
    @Column(length = 20)  private String apartmentNumber;
    @Column(length = 10)  private String floor;
}
