package com.example.scan_dineCustomer.table.entity;

import com.example.scan_dineCustomer.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dine_order", schema = "tables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DineOrder {

    @Id
    @Column(length = 15)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private TableSession session;

    // Denormalized for easy captain queries — avoids joining through session
    @Column(name = "restaurant_id", nullable = false, length = 15)
    private String restaurantId;

    @Column(name = "table_id", nullable = false, length = 15)
    private String tableId;

    @Column(name = "table_number", length = 20)
    private String tableNumber;

    @Column(name = "customer_id", nullable = false, length = 15)
    private String customerId;

    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Column(name = "customer_mobile", length = 15)
    private String customerMobile;

    @Column(name = "captain_id", length = 15)
    private String captainId;

    @Column(name = "captain_name", length = 100)
    private String captainName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(length = 500)
    private String notes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "preparing_started_at")
    private Instant preparingStartedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    private void generateId() {
        if (id == null || id.isEmpty()) {
            id = "ORD_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        }
    }
}
