// src/main/java/com/empresa/ecommerce_backend/dto/response/BillingProfileResponse.java
package com.empresa.ecommerce_backend.dto.response;

import com.empresa.ecommerce_backend.enums.DocumentType;
import com.empresa.ecommerce_backend.enums.TaxCondition;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class BillingProfileResponse {
    private Long id;

    private DocumentType documentType;
    private String documentNumber;

    private TaxCondition taxCondition;
    private String businessName;
    private String emailForInvoices;
    private String phone;

    private Long billingAddressId;
    private boolean defaultProfile; // ✅ expuesto así (no isDefault)
}
