package com.empresa.ecommerce_backend.enums;

public enum PaymentStatus {
    INITIATED,       // generado, esperando que el user pague
    PENDING,         // provider lo marcó pendiente
    APPROVED,        // pagado
    REJECTED,        // rechazado
    CANCELED,        // cancelado por el user o timeout
    EXPIRED          // vencido
}