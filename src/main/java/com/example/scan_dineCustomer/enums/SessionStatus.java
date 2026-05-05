package com.example.scan_dineCustomer.enums;

public enum SessionStatus {
    ACTIVE,   // table is occupied, orders can be placed
    BILLED,   // bill requested, no new orders
    CLOSED    // session ended, table cleared
}
