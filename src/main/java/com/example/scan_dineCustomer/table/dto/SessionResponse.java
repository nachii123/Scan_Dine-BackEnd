package com.example.scan_dineCustomer.table.dto;

import com.example.scan_dineCustomer.enums.SessionStatus;
import com.example.scan_dineCustomer.table.entity.TableSession;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class SessionResponse {
    private String id;
    private String tableId;
    private String tableNumber;
    private String restaurantId;
    private int customerCount;
    private Instant startTime;
    private SessionStatus status;
    private boolean billRequested;
    private Instant billRequestedAt;
    private boolean waiterCalled;
    private Instant waiterCalledAt;

    public static SessionResponse from(TableSession s) {
        return SessionResponse.builder()
                .id(s.getId())
                .tableId(s.getTable().getId())
                .tableNumber(s.getTable().getTableNumber())
                .restaurantId(s.getRestaurantId())
                .customerCount(s.getCustomerCount())
                .startTime(s.getStartTime())
                .status(s.getStatus())
                .billRequested(s.isBillRequested())
                .billRequestedAt(s.getBillRequestedAt())
                .waiterCalled(s.isWaiterCalled())
                .waiterCalledAt(s.getWaiterCalledAt())
                .build();
    }
}
