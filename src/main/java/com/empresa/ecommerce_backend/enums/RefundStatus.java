// src/main/java/com/empresa/ecommerce_backend/enums/RefundStatus.java
package com.empresa.ecommerce_backend.enums;

public enum RefundStatus {
    REQUESTED,   // pedido por el usuario/admin
    PROCESSING,  // enviado al gateway/banco
    COMPLETED,   // dinero devuelto
    FAILED,      // rechazado o error
    CANCELED     // cancelado antes de completarse
}
