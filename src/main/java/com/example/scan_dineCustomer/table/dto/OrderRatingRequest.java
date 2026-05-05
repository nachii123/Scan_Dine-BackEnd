package com.example.scan_dineCustomer.table.dto;

import lombok.Data;

@Data
public class OrderRatingRequest {
    private int rating; // 1-5
    private String comment;
}
