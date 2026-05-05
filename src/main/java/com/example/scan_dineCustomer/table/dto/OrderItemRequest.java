package com.example.scan_dineCustomer.table.dto;

import lombok.Data;

@Data
public class OrderItemRequest {
    private String menuItemId;
    private int quantity;
    private String notes;
}
