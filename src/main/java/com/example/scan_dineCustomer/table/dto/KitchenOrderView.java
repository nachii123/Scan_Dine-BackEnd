package com.example.scan_dineCustomer.table.dto;

import com.example.scan_dineCustomer.enums.OrderItemStatus;
import com.example.scan_dineCustomer.enums.OrderStatus;
import com.example.scan_dineCustomer.table.entity.DineOrder;
import lombok.Data;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
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
                .map(i -> {
                    KitchenItemView item = new KitchenItemView();
                    item.setOrderItemId(i.getId());
                    item.setMenuItemName(i.getMenuItemName());
                    item.setQuantity(i.getQuantity());
                    item.setNotes(i.getNotes());
                    item.setStatus(i.getStatus());
                    return item;
                })
                .collect(Collectors.toList());
        KitchenOrderView view = new KitchenOrderView();
        view.setOrderId(order.getId());
        view.setTableId(order.getTableId());
        view.setRestaurantId(order.getRestaurantId());
        view.setStatus(order.getStatus());
        view.setPreparingStartedAt(order.getPreparingStartedAt());
        view.setPreparingForMinutes(preparingFor);
        view.setOverdue(preparingFor > estimatedMinutes && estimatedMinutes > 0);
        view.setItems(items);
        return view;
    }
}
