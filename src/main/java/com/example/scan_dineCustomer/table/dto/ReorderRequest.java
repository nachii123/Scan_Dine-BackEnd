package com.example.scan_dineCustomer.table.dto;

import lombok.Data;

@Data
public class ReorderRequest {
    private String previousOrderId;
    private String sessionId;
    private String restaurantId;
}
