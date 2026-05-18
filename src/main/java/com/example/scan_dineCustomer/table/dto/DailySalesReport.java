package com.example.scan_dineCustomer.table.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class DailySalesReport {
    private LocalDate date;
    private String restaurantId;
    private BigDecimal totalRevenue;
    private int totalOrders;
    private int totalItemsSold;
    private List<TopItemStats> topItems;
    private List<HourlyStats> revenueByHour;

    @Data
    public static class TopItemStats {
        private String menuItemId;
        private String menuItemName;
        private int quantitySold;
        private BigDecimal revenue;
    }

    @Data
    public static class HourlyStats {
        private int hour;
        private int orderCount;
        private BigDecimal revenue;
    }
}
