package com.example.scan_dineCustomer.enums;

public enum TableStatus {
    AVAILABLE,   // empty, ready for guests
    OCCUPIED,    // guests seated, no order yet
    BILLED,      // order placed, payment pending
    RESERVED,    // future reservation
    INACTIVE     // table closed / under maintenance
}