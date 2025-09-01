package com.empresa.ecommerce_backend.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentSummaryResponse {
    private String method;
    private String status;
    private BigDecimal amount;
    private String redirectUrl;
}