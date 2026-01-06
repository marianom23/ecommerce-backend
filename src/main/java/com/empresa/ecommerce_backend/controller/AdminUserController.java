package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.response.PaginatedResponse;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.dto.response.UserAdminResponse;
import com.empresa.ecommerce_backend.repository.projection.OrderSummaryProjection;
import com.empresa.ecommerce_backend.service.interfaces.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ServiceResult<PaginatedResponse<UserAdminResponse>> getUsers(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return adminUserService.getUsers(query, pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ServiceResult<UserAdminResponse> getUserById(@PathVariable Long id) {
        return adminUserService.getUserById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/orders")
    public ServiceResult<PaginatedResponse<OrderSummaryProjection>> getUserOrders(
            @PathVariable Long id,
            @PageableDefault(size = 10, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return adminUserService.getUserOrders(id, pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/toggle-status")
    public ServiceResult<Void> toggleUserStatus(@PathVariable Long id) {
        return adminUserService.toggleUserStatus(id);
    }
}
