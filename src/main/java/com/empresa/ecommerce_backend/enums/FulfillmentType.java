package com.empresa.ecommerce_backend.enums;

public enum FulfillmentType {
    PHYSICAL,           // requiere envío y stock físico
    DIGITAL_ON_DEMAND,  // no hay stock, se compra luego y se entrega manualmente
    DIGITAL_INSTANT     // clave digital disponible de inmediato (entrega automática)
}
