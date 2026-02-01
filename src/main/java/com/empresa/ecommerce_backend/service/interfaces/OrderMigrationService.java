package com.empresa.ecommerce_backend.service.interfaces;

public interface OrderMigrationService {
    /**
     * Busca órdenes creadas como guest con el email dado y las asigna al nuevo
     * userId.
     * 
     * @param email  Email del usuario
     * @param userId ID del usuario registrado
     * @return Cantidad de órdenes migradas
     */
    int migrateGuestOrdersToUser(String email, Long userId);
}
