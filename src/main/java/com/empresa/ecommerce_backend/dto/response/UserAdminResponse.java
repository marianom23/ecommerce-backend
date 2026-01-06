package com.empresa.ecommerce_backend.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class UserAdminResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String authProvider; // LOCAL, GOOGLE
    private boolean verified;
    private List<String> roles;
    private int orderCount; // Opcional: mostrar cuántas órdenes tiene
}
