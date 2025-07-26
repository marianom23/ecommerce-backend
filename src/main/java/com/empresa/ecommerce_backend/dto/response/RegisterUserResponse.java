// src/main/java/com/empresa/ecommerce_backend/dto/response/RegisterUserResponse.java
package com.empresa.ecommerce_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterUserResponse {
    private Long id;
    private String email;
}
