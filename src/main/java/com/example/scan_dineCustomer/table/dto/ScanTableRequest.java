package com.example.scan_dineCustomer.table.dto;

import lombok.Data;

@Data
public class ScanTableRequest {
    private String restaurantId;
    private String tableId;
    private int customerCount;
}
