package com.empresa.ecommerce_backend.dto.response;

import lombok.Data;
import java.util.Set;

@Data
public class UserMeResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private boolean verified;
    private Set<String> roles;
}
