// src/main/java/com/empresa/ecommerce_backend/enums/ChargebackStatus.java
package com.empresa.ecommerce_backend.enums;

public enum ChargebackStatus {
    RECEIVED,   // llegó la notificación del banco/gateway
    DISPUTED,   // presentada la defensa
    WON,        // ganaste la disputa (no te sacan el dinero)
    LOST,       // perdiste la disputa (pierdes el dinero)
    CANCELED    // se canceló antes de resolverse
}
