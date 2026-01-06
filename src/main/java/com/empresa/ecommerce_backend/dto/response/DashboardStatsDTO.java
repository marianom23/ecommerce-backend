package com.empresa.ecommerce_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    // Card 1: Customers
    private Long totalCustomers;
    private Double customerGrowth; // % vs mes anterior (opcional)

    // Card 2: Orders
    private Long totalOrders;
    private Double orderGrowth; // % vs mes anterior (opcional)

    // Card 3: Monthly Sales / Earnings
    private BigDecimal totalRevenue; // Revenue total histórico o del mes actual
    private BigDecimal monthlyTarget; // Un target fijo o calculado (mockeado por ahora)
    private Double revenueGrowth; // % vs mes anterior

    // Gráfico de Barras: Ventas por Mes (últimos 12 meses)
    private List<MonthlySalesDTO> salesByMonth;
    
    // Gráfico de Mapa: Ventas por Provincia
    private List<ProvinceStatDTO> salesByProvince;

    @Data
    @AllArgsConstructor
    public static class MonthlySalesDTO {
        private String month; // "Jan", "Feb", etc.
        private BigDecimal totalSales;
        private Long orderCount;
    }
}
