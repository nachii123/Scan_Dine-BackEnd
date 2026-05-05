package com.example.scan_dineCustomer.enums;

public enum OrderItemStatus {
    PENDING,    // waiting for captain action
    ACCEPTED,   // captain accepted
    PREPARING,  // kitchen actively cooking this item
    READY,      // item ready, waiting for captain to serve
    REJECTED,   // captain rejected (e.g., unavailable)
    SERVED,     // delivered to the table
    CANCELLED   // cancelled
}

