// src/main/java/com/empresa/ecommerce_backend/enums/PaymentAttemptStatus.java
package com.empresa.ecommerce_backend.enums;

public enum PaymentAttemptStatus {
    REQUESTED,   // se envió al gateway
    SUCCESS,     // aprobado / autorizado
    FAILED,      // error definitivo
    RETRY        // falló pero se reintentará
}
