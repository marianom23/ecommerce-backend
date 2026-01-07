package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.dto.response.ProvinceStatDTO;
import com.empresa.ecommerce_backend.dto.response.ServiceResult;
import com.empresa.ecommerce_backend.repository.OrderRepository;
import com.empresa.ecommerce_backend.service.interfaces.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final com.empresa.ecommerce_backend.repository.UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<List<ProvinceStatDTO>> getSalesByProvince() {
        List<Object[]> results = orderRepository.countAndSumByState();
        return ServiceResult.ok(mapProvinces(results));
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResult<com.empresa.ecommerce_backend.dto.response.DashboardStatsDTO> getDashboardStats() {
        com.empresa.ecommerce_backend.dto.response.DashboardStatsDTO stats = new com.empresa.ecommerce_backend.dto.response.DashboardStatsDTO();

        try {
            // 1. Clientes Totales
            long totalCustomers = userRepository.countByRoles_Name("ROLE_USER");
            stats.setTotalCustomers(totalCustomers);
        } catch (Exception e) {
            stats.setTotalCustomers(0L);
        }
        stats.setCustomerGrowth(10.0); // Mock por ahora

        try {
            // 2. Órdenes Totales
            long totalOrders = orderRepository.count();
            stats.setTotalOrders(totalOrders);
        } catch (Exception e) {
            stats.setTotalOrders(0L);
        }
        stats.setOrderGrowth(5.5); // Mock

        try {
            // 3. Ventas Mensuales (Gráfico barras)
            List<Object[]> monthlyData = orderRepository.findMonthlySales();
            
            List<com.empresa.ecommerce_backend.dto.response.DashboardStatsDTO.MonthlySalesDTO> salesByMonth = monthlyData.stream()
                    .limit(12)
                    .map(row -> new com.empresa.ecommerce_backend.dto.response.DashboardStatsDTO.MonthlySalesDTO(
                            getMonthName((Integer) row[0]), 
                            (BigDecimal) row[3], 
                            ((Number) row[2]).longValue()
                    ))
                    .collect(Collectors.toList());
            java.util.Collections.reverse(salesByMonth);
            stats.setSalesByMonth(salesByMonth);

            // 4. Total Revenue
            BigDecimal totalRev = monthlyData.stream()
                    .map(row -> (BigDecimal) row[3])
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.setTotalRevenue(totalRev);
        } catch (Exception e) {
            stats.setSalesByMonth(List.of());
            stats.setTotalRevenue(BigDecimal.ZERO);
        }
        
        stats.setMonthlyTarget(new BigDecimal("20000.00"));
        stats.setRevenueGrowth(12.3);

        try {
            // 5. Mapa (Provincias)
            stats.setSalesByProvince(mapProvinces(orderRepository.countAndSumByState()));
        } catch (Exception e) {
            stats.setSalesByProvince(List.of());
        }

        return ServiceResult.ok(stats);
    }

    private List<ProvinceStatDTO> mapProvinces(List<Object[]> results) {
        return results.stream()
                .map(row -> new ProvinceStatDTO(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        (BigDecimal) row[2]
                ))
                .collect(Collectors.toList());
    }

    private String getMonthName(Integer month) {
        if (month == null) return "";
        return java.time.Month.of(month).getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.US);
    }
}
