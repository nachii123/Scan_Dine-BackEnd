package com.example.scan_dineCustomer.table.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class EstimatedWaitResponse {
    private String orderId;
    private int estimatedMinutes;
    private long preparingForMinutes; // how long already preparing, 0 if not yet
    private int remainingMinutes;
    private List<ItemWaitInfo> items;

    @Data
    @Builder
    public static class ItemWaitInfo {
        private String menuItemName;
        private int quantity;
        private int prepTimeMinutes;
    }
}
