package com.example.scan_dineCustomer.table.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class KotResponse {

    private String kotNumber;       // same as orderId
    private String tableNumber;
    private String restaurantId;
    private String captainId;
    private String captainName;
    private Instant generatedAt;
    private List<KotItem> items;

    @Data
    @Builder
    public static class KotItem {
        private String menuItemName;
        private int quantity;
        private String notes;
    }
}
