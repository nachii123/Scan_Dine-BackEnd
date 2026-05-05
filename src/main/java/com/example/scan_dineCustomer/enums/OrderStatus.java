package com.example.scan_dineCustomer.enums;

public enum OrderStatus {
    PENDING,             // placed by customer, waiting for captain
    ACCEPTED,            // captain accepted the order
    PARTIALLY_ACCEPTED,  // some items accepted, some rejected
    PREPARING,           // kitchen is actively preparing the order
    READY,               // food is ready, waiting to be picked up by captain
    SERVED,              // all items delivered to table
    CANCELLED            // cancelled by captain or customer
}
