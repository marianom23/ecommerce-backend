package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.dto.response.PaginatedResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.dto.response.UserAdminResponse;
import com.empresa.ecommerce_backend.repository.projection.OrderSummaryProjection;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {

    // Listar usuarios (con búsqueda opcional)
    ServiceResult<PaginatedResponse<UserAdminResponse>> getUsers(String query, Pageable pageable);

    // Obtener detalle de un usuario
    ServiceResult<UserAdminResponse> getUserById(Long id);

    // Obtener órdenes de un usuario (para ver su historial desde admin)
    ServiceResult<PaginatedResponse<OrderSummaryProjection>> getUserOrders(Long userId, Pageable pageable);
    
    // Toggle active/verified status (opcional)
    ServiceResult<Void> toggleUserStatus(Long userId);
}
