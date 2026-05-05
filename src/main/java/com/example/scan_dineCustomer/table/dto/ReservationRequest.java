package com.example.scan_dineCustomer.table.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class ReservationRequest {
    private String restaurantId;
    private String tableId;
    private String customerName;
    private String customerMobile;
    private int partySize;
    private Instant reservedFor;
    private String notes;
}
