package com.example.scan_dineCustomer.table.dto;

import lombok.Data;

@Data
public class AddToCartRequest {
    private String sessionId;
    private String restaurantId;
    private String menuItemId;
    private int quantity;
    private String notes;
}
