package com.empresa.ecommerce_backend.service.interfaces;

import com.empresa.ecommerce_backend.dto.response.ProvinceStatDTO;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;

import java.util.List;

public interface DashboardService {
    ServiceResult<List<ProvinceStatDTO>> getSalesByProvince();

    ServiceResult<com.empresa.ecommerce_backend.dto.response.DashboardStatsDTO> getDashboardStats();
}
