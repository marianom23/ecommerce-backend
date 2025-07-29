package com.empresa.ecommerce_backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PurchaseOrderResponse {
    private Long id;
    private LocalDateTime purchaseDate;
    private String notes;
    private List<PurchaseLotResponse> lots;
}
