package com.example.scan_dineCustomer.table.dto;

import com.example.scan_dineCustomer.enums.OrderItemStatus;
import com.example.scan_dineCustomer.enums.OrderStatus;
import com.example.scan_dineCustomer.table.entity.DineOrder;
import lombok.Builder;
import lombok.Data;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class KitchenOrderView {
    private String orderId;
    private String tableId;
    private String restaurantId;
    private OrderStatus status;
    private Instant preparingStartedAt;
    private long preparingForMinutes;
    private boolean overdue;
    private List<KitchenItemView> items;

    @Data
    @Builder
    public static class KitchenItemView {
        private String orderItemId;
        private String menuItemName;
        private int quantity;
        private String notes;
        private OrderItemStatus status;
    }

    public static KitchenOrderView from(DineOrder order, int estimatedMinutes) {
        long preparingFor = order.getPreparingStartedAt() != null
                ? Duration.between(order.getPreparingStartedAt(), Instant.now()).toMinutes()
                : 0;
        List<KitchenItemView> items = order.getItems().stream()
                .filter(i -> i.getStatus() != OrderItemStatus.REJECTED && i.getStatus() != OrderItemStatus.CANCELLED)
                .map(i -> KitchenItemView.builder()
                        .orderItemId(i.getId())
                        .menuItemName(i.getMenuItemName())
                        .quantity(i.getQuantity())
                        .notes(i.getNotes())
                        .status(i.getStatus())
                        .build())
                .collect(Collectors.toList());
        return KitchenOrderView.builder()
                .orderId(order.getId())
                .tableId(order.getTableId())
                .restaurantId(order.getRestaurantId())
                .status(order.getStatus())
                .preparingStartedAt(order.getPreparingStartedAt())
                .preparingForMinutes(preparingFor)
                .overdue(preparingFor > estimatedMinutes && estimatedMinutes > 0)
                .items(items)
                .build();
    }
}
