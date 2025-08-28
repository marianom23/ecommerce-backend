// src/main/java/com/empresa/ecommerce_backend/dto/response/AddressResponse.java
package com.empresa.ecommerce_backend.dto.response;

import com.empresa.ecommerce_backend.enums.AddressType;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class AddressResponse {
    private Long id;
    private String street;
    private String streetNumber;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private AddressType type;
    private String apartmentNumber;
    private String floor;
    private LocalDateTime lastUsedAt;
}
