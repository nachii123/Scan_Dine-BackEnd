package com.example.scan_dineCustomer.table.dto;

import com.example.scan_dineCustomer.enums.OrderStatus;
import com.example.scan_dineCustomer.table.entity.DineOrder;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class OrderResponse{
    private String id;
    private String sessionId;
    private String tableId;
    private String restaurantId;
    private String customerId;
    private String captainId;
    private String captainName;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String notes;
    private Instant createdAt;
    private List<OrderItemResponse> items;

    public static OrderResponse from(DineOrder order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(OrderItemResponse::from)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .sessionId(order.getSession().getId())
                .tableId(order.getTableId())
                .restaurantId(order.getRestaurantId())
                .customerId(order.getCustomerId())
                .captainId(order.getCaptainId())
                .captainName(order.getCaptainName())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .items(itemResponses)
                .build();
    }
}
