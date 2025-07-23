// src/main/java/com/empresa/ecommerce_backend/enums/InventoryMovementType.java
package com.empresa.ecommerce_backend.enums;

public enum InventoryMovementType {
    SALE,        // salida por venta
    RETURN,      // ingreso por devolución
    RESTOCK,     // ingreso por reposición
    ADJUSTMENT,  // corrección manual
    RESERVATION, // reserva (bloqueo temporal)
    RELEASE      // liberación de reserva
}
