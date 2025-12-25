package com.empresa.ecommerce_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankAccountRequest {

    @NotBlank
    private String bankName;

    @NotBlank
    private String holderName;

    @NotBlank
    private String cbu;

    private String alias;

    @NotBlank
    private String accountNumber;

    private boolean active = true;
}
