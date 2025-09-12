// src/main/java/com/empresa/ecommerce_backend/dto/request/BillingProfileRequest.java
package com.empresa.ecommerce_backend.dto.request;

import com.empresa.ecommerce_backend.enums.DocumentType;
import com.empresa.ecommerce_backend.enums.TaxCondition;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BillingProfileRequest {

    @NotBlank
    @Size(max = 150)
    private String fullName; // ✅ nuevo

    @NotNull
    private DocumentType documentType;

    @NotBlank
    @Size(max = 20)
    private String documentNumber;

    @NotNull
    private TaxCondition taxCondition;

    @Size(max = 150)
    private String businessName;

    @Email
    @Size(max = 150)
    private String emailForInvoices;

    @Size(max = 50)
    private String phone;

    @NotNull
    private Long billingAddressId;

    private Boolean isDefault;
}
