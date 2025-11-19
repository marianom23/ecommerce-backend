package com.empresa.ecommerce_backend.service.interfaces;

public interface OrderCleanupService {

    /**
     * Cancela órdenes expiradas (PENDING y con expiresAt < now).
     * Este método lo ejecuta el scheduler automáticamente.
     */
    void cancelExpiredOrders();
}
