// src/main/java/com/empresa/ecommerce_backend/dto/response/LoginResponse.java
package com.empresa.ecommerce_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor           // ⬅️ necesario para que MapStruct cree la instancia y use setters
@AllArgsConstructor
public class LoginResponse {
    String token;
    Instant expiresAt;
    String tokenType;        // "Bearer"

    Long id;
    String email;
    String firstName;
    String lastName;
    String fullName;         // derivado
    boolean verified;
    String provider;         // LOCAL | GOOGLE | AZURE_AD...
    List<String> roles;      // lista de roles
}
