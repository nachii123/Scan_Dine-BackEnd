package com.example.scan_dineCustomer.table.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class BillResponse {

    private String billNumber;       // same as sessionId
    private String sessionId;
    private String tableNumber;
    private String restaurantId;
    private String restaurantName;
    private String gstin;
    private String currency;
    private int customerCount;
    private String captainId;
    private String captainName;
    private Instant sessionStartTime;
    private Instant generatedAt;
    private List<BillItem> items;
    private BigDecimal subtotal;

    @Data
    @Builder
    public static class BillItem {
        private String orderId;
        private String menuItemName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
}
