package com.example.scan_dineCustomer.table.dto;

import lombok.Data;

@Data
public class CreateTableRequest {
    private String restaurantId;
    private String tableNumber;
    private int capacity;
    private String floor;
}
