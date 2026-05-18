package com.example.scan_dineCustomer.table.dto;

import com.example.scan_dineCustomer.enums.SessionStatus;
import com.example.scan_dineCustomer.enums.TableStatus;
import com.example.scan_dineCustomer.restaurant.dto.BaseResponse;
import com.example.scan_dineCustomer.table.entity.RestaurantTable;
import com.example.scan_dineCustomer.table.entity.TableSession;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionResponse extends BaseResponse {
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
    private boolean canPlaceOrder;
    private boolean sessionCreated;
    private String tableStatus;

    public static SessionResponse from(TableSession s) {
        SessionResponse response = new SessionResponse();
        response.setId(s.getId());
        response.setTableId(s.getTable().getId());
        response.setTableNumber(s.getTable().getTableNumber());
        response.setRestaurantId(s.getRestaurantId());
        response.setCustomerCount(s.getCustomerCount());
        response.setStartTime(s.getStartTime());
        response.setStatus(s.getStatus());
        response.setBillRequested(s.isBillRequested());
        response.setBillRequestedAt(s.getBillRequestedAt());
        response.setWaiterCalled(s.isWaiterCalled());
        response.setWaiterCalledAt(s.getWaiterCalledAt());
        response.setTableStatus(s.getTable().getStatus().name());
        return response;
    }

    public static SessionResponse scanSuccess(TableSession s) {
        SessionResponse response = from(s);
        response.setEs(0);
        response.setStatusCode(200);
        response.setMessage("Table scanned successfully. Session created.");
        response.setCanPlaceOrder(true);
        response.setSessionCreated(true);
        response.setTableStatus(TableStatus.OCCUPIED.name());
        return response;
    }

    public static SessionResponse tableOccupied(RestaurantTable table) {
        SessionResponse response = new SessionResponse();
        response.setEs(1);
        response.setStatusCode(409);
        response.setMessage("This table is already occupied. Please wait until checkout is completed.");
        response.setTableId(table.getId());
        response.setTableNumber(table.getTableNumber());
        response.setRestaurantId(table.getRestaurantId());
        response.setCanPlaceOrder(false);
        response.setSessionCreated(false);
        response.setTableStatus(TableStatus.OCCUPIED.name());
        return response;
    }
}
