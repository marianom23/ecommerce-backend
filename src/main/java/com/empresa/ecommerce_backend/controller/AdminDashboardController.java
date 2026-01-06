package com.empresa.ecommerce_backend.controller;

import com.empresa.ecommerce_backend.dto.response.ProvinceStatDTO;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.service.interfaces.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/provinces")
    public ServiceResult<List<ProvinceStatDTO>> getSalesByProvince() {
        return dashboardService.getSalesByProvince();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats")
    public ServiceResult<com.empresa.ecommerce_backend.dto.response.DashboardStatsDTO> getDashboardStats() {
        return dashboardService.getDashboardStats();
    }
}
