package com.example.scan_dineCustomer.table.dto;

import com.example.scan_dineCustomer.enums.OrderStatus;
import com.example.scan_dineCustomer.table.entity.DineOrder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderResponse{
    private String id;
    private String sessionId;
    private String tableId;
    private String tableNumber;
    private String restaurantId;
    private String customerId;
    private String customerName;
    private String customerMobile;
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

        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setSessionId(order.getSession().getId());
        response.setTableId(order.getTableId());
        response.setTableNumber(order.getTableNumber());
        response.setRestaurantId(order.getRestaurantId());
        response.setCustomerId(order.getCustomerId());
        response.setCustomerName(order.getCustomerName());
        response.setCustomerMobile(order.getCustomerMobile());
        response.setCaptainId(order.getCaptainId());
        response.setCaptainName(order.getCaptainName());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setNotes(order.getNotes());
        response.setCreatedAt(order.getCreatedAt());
        response.setItems(itemResponses);
        return response;
    }
}
