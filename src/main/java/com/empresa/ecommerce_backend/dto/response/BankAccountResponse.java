package com.empresa.ecommerce_backend.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankAccountResponse {
    private Long id;
    private String bankName;
    private String holderName;
    private String cbu;
    private String alias;
    private String accountNumber;
    private boolean active;
}
