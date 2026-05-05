package com.example.scan_dineCustomer.table.dto;

import com.example.scan_dineCustomer.enums.TableStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CaptainTableView {
    private String id;
    private String tableNumber;
    private int capacity;
    private TableStatus status;
    private String floor;
    private String qrCode;
    private boolean active;
    private String activeSessionId;   // null when AVAILABLE
    private long pendingOrderCount;   // 0 when AVAILABLE
}
