// src/main/java/com/empresa/ecommerce_backend/dto/request/UpdateBillingProfileRequest.java
package com.empresa.ecommerce_backend.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBillingProfileRequest {
    // Para usuarios autenticados: usar BillingProfile de BD
    private Long billingProfileId;

    // Para guests: datos mínimos requeridos
    private String fullName;
    private String email;
    private String phone;

    private com.empresa.ecommerce_backend.enums.DocumentType documentType;
    private String documentNumber;
    private com.empresa.ecommerce_backend.enums.TaxCondition taxCondition;
    private String businessName;

    // Opcionales para factura completa (guests con productos físicos)
    private String street;
    private String streetNumber;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String apartmentNumber;
    private String floor;
}
