// src/main/java/com/empresa/ecommerce_backend/dto/request/UpdateShippingAddressRequest.java
package com.empresa.ecommerce_backend.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateShippingAddressRequest {
    // Para usuarios autenticados: usar Address de BD
    private Long shippingAddressId;

    // Para guests: enviar datos directos
    private String street;
    private String streetNumber;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String apartmentNumber;
    private String floor;

    // Opcionales para el snapshot
    private String recipientName;
    private String phone;
}
