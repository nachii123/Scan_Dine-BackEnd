package com.example.scan_dineCustomer.table.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_rating", schema = "tables")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class OrderRating {

    @Id
    @Column(length = 15)
    private String id;

    @Column(name = "order_id", nullable = false, length = 15)
    private String orderId;

    @Column(name = "customer_id", nullable = false, length = 15)
    private String customerId;

    @Column(name = "restaurant_id", nullable = false, length = 15)
    private String restaurantId;

    @Column(nullable = false)
    private int rating; // 1-5

    @Column(length = 500)
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    private void generateId() {
        if (id == null || id.isEmpty()) {
            id = "RAT_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        }
    }
}
