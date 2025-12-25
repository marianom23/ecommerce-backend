package com.empresa.ecommerce_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupplierRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    private String email;
    private String phone;
    private String address;
}
