package com.example.scan_dineCustomer.table.dto;

import lombok.Data;

import java.util.List;

@Data
public class PlaceOrderRequest {
    private String sessionId;
    private String restaurantId;
    private String notes;
    private List<OrderItemRequest> items;
}
