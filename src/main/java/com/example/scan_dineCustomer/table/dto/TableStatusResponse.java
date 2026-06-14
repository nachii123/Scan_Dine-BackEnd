package com.example.scan_dineCustomer.table.dto;

import com.example.scan_dineCustomer.enums.TableStatus;
import com.example.scan_dineCustomer.table.entity.RestaurantTable;
import lombok.Data;

@Data
public class TableStatusResponse {
    private String tableId;
    private String restaurantId;
    private String tableNumber;
    private int capacity;
    private TableStatus status;
    private boolean active;
    private boolean occupied;
    private boolean canPlaceOrder;

    public static TableStatusResponse from(RestaurantTable table, boolean canPlaceOrder) {
        TableStatusResponse response = new TableStatusResponse();
        response.setTableId(table.getId());
        response.setRestaurantId(table.getRestaurantId());
        response.setTableNumber(table.getTableNumber());
        response.setCapacity(table.getCapacity());
        response.setStatus(table.getStatus());
        response.setActive(table.isActive());
        response.setOccupied(table.getStatus() == TableStatus.OCCUPIED);
        response.setCanPlaceOrder(canPlaceOrder);
        return response;
    }
}
