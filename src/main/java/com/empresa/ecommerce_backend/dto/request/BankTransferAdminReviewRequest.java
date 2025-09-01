package com.empresa.ecommerce_backend.dto.request;

import lombok.Data;

@Data
public class BankTransferAdminReviewRequest {
    private boolean approve;
    private String note;
}