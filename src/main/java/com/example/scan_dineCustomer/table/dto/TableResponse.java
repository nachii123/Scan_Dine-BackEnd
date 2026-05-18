package com.example.scan_dineCustomer.table.dto;

import com.example.scan_dineCustomer.enums.TableStatus;
import com.example.scan_dineCustomer.table.entity.RestaurantTable;
import lombok.Data;

@Data
public class TableResponse {
    private String id;
    private String restaurantId;
    private String tableNumber;
    private int capacity;
    private TableStatus status;
    private String floor;
    private String qrCode;
    private boolean active;

    public static TableResponse from(RestaurantTable t) {
        TableResponse response = new TableResponse();
        response.setId(t.getId());
        response.setRestaurantId(t.getRestaurantId());
        response.setTableNumber(t.getTableNumber());
        response.setCapacity(t.getCapacity());
        response.setStatus(t.getStatus());
        response.setFloor(t.getFloor());
        response.setQrCode(t.getQrCode());
        response.setActive(t.isActive());
        return response;
    }
}
