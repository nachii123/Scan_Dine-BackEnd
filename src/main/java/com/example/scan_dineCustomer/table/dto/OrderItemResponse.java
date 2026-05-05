package com.example.scan_dineCustomer.table.dto;

import com.example.scan_dineCustomer.enums.OrderItemStatus;
import com.example.scan_dineCustomer.table.entity.OrderItem;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    private String id;
    private String menuItemId;
    private String menuItemName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private OrderItemStatus status;
    private String notes;
    private String imageUrl;

    public static OrderItemResponse from(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .menuItemId(item.getMenuItemId())
                .menuItemName(item.getMenuItemName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .status(item.getStatus())
                .notes(item.getNotes())
                .imageUrl(item.getImageUrl())
                .build();
    }
}
