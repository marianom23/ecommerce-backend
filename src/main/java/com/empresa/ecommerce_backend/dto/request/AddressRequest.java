// src/main/java/com/empresa/ecommerce_backend/dto/request/AddressRequest.java
package com.empresa.ecommerce_backend.dto.request;

import com.empresa.ecommerce_backend.enums.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AddressRequest {

    @NotBlank
    private String street;

    private String streetNumber;

    @NotBlank
    private String city;

    private String state;

    private String postalCode;

    @NotBlank
    private String country;

    @NotNull
    private AddressType type;

    private String apartmentNumber;
    private String floor;
}
