package com.example.scan_dineCustomer.table.dto;

import com.example.scan_dineCustomer.table.entity.OrderRating;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class OrderRatingResponse {
    private String id;
    private String orderId;
    private String customerId;
    private String restaurantId;
    private int rating;
    private String comment;
    private Instant createdAt;

    public static OrderRatingResponse from(OrderRating r) {
        return OrderRatingResponse.builder()
                .id(r.getId())
                .orderId(r.getOrderId())
                .customerId(r.getCustomerId())
                .restaurantId(r.getRestaurantId())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
