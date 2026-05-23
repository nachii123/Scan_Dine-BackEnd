package com.example.scan_dineCustomer.table.dto;

import lombok.Data;

import java.util.List;

@Data
public class GuestOrderRequest {
    private String sessionId;
    private String restaurantId;
    private String guestName;
    private String guestMobile;
    private String notes;
    private List<OrderItemRequest> items;
}
