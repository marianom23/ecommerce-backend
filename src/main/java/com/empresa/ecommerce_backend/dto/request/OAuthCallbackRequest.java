package com.empresa.ecommerce_backend.dto.request;

import lombok.Data;

@Data
public class OAuthCallbackRequest {
    private String email;
    private String firstName;
    private String lastName;
    private String provider; // "google" o "azure-ad"
    private String idToken;  // token para verificar autenticidad
}
