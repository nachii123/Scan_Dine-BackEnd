package com.example.scan_dineCustomer.table.dto;

import com.example.scan_dineCustomer.table.entity.OrderRating;
import lombok.Data;
import java.time.Instant;

@Data
public class OrderRatingResponse {
    private String id;
    private String orderId;
    private String customerId;
    private String restaurantId;
    private int rating;
    private String comment;
    private Instant createdAt;

    public static OrderRatingResponse from(OrderRating r) {
        OrderRatingResponse response = new OrderRatingResponse();
        response.setId(r.getId());
        response.setOrderId(r.getOrderId());
        response.setCustomerId(r.getCustomerId());
        response.setRestaurantId(r.getRestaurantId());
        response.setRating(r.getRating());
        response.setComment(r.getComment());
        response.setCreatedAt(r.getCreatedAt());
        return response;
    }
}
