// src/main/java/com/empresa/ecommerce_backend/dto/response/BillingProfileResponse.java
package com.empresa.ecommerce_backend.dto.response;

import com.empresa.ecommerce_backend.enums.DocumentType;
import com.empresa.ecommerce_backend.enums.TaxCondition;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class BillingProfileResponse {
    private Long id;

    private String fullName; // ✅ nuevo

    private DocumentType documentType;
    private String documentNumber;

    private TaxCondition taxCondition;
    private String businessName;
    private String emailForInvoices;
    private String phone;

    private String street;
    private String streetNumber;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String apartmentNumber;
    private String floor;
    private boolean defaultProfile;
}