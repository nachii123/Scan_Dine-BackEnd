package com.example.scan_dineCustomer.table.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cart", schema = "tables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @Column(length = 15)
    private String id;

    @Column(name = "session_id", nullable = false, length = 15)
    private String sessionId;

    @Column(name = "restaurant_id", nullable = false, length = 15)
    private String restaurantId;

    @Column(name = "customer_id", nullable = false, length = 15)
    private String customerId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CartItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    private void generateId() {
        if (id == null || id.isEmpty()) {
            id = "CRT_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        }
    }
}
