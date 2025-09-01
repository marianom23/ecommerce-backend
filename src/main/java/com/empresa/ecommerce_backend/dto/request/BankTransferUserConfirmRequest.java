// src/main/java/com/empresa/ecommerce_backend/dto/request/BankTransferUserConfirmRequest.java
package com.empresa.ecommerce_backend.dto.request;

import lombok.Data;

@Data
public class BankTransferUserConfirmRequest {
    private String reference;  // nro de operación
    private String receiptUrl; // URL del comprobante (opcional)
}
