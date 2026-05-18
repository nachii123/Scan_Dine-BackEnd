package com.example.scan_dineCustomer.table.dto;

import lombok.Data;

@Data
public class UpdateCartItemRequest {
    private int quantity;
    private String notes;
}
