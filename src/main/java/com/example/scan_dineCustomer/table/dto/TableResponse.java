package com.example.scan_dineCustomer.table.dto;

import com.example.scan_dineCustomer.enums.TableStatus;
import com.example.scan_dineCustomer.restaurant.dto.BaseResponse;
import com.example.scan_dineCustomer.table.entity.RestaurantTable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
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
        return TableResponse.builder()
                .id(t.getId())
                .restaurantId(t.getRestaurantId())
                .tableNumber(t.getTableNumber())
                .capacity(t.getCapacity())
                .status(t.getStatus())
                .floor(t.getFloor())
                .qrCode(t.getQrCode())
                .active(t.isActive())
                .build();
    }
}
