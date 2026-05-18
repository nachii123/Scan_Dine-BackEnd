package com.example.scan_dineCustomer.table.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class KotResponse {

    private String kotNumber;       // same as orderId
    private String tableNumber;
    private String restaurantId;
    private String captainId;
    private String captainName;
    private Instant generatedAt;
    private List<KotItem> items;

    @Data
    public static class KotItem {
        private String menuItemName;
        private int quantity;
        private String notes;
    }
}
