package com.example.scan_dineCustomer.table.entity;

import com.example.scan_dineCustomer.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "table_session", schema = "tables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TableSession {

    @Id
    @Column(length = 15)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private RestaurantTable table;

    @Column(name = "restaurant_id", nullable = false, length = 15)
    private String restaurantId;

    @Column(name = "customer_count", nullable = false)
    private int customerCount = 1;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status = SessionStatus.ACTIVE;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DineOrder> orders = new ArrayList<>();

    @Column(name = "bill_requested", nullable = false)
    private boolean billRequested = false;

    @Column(name = "bill_requested_at")
    private Instant billRequestedAt;

    @Column(name = "waiter_called", nullable = false)
    private boolean waiterCalled = false;

    @Column(name = "waiter_called_at")
    private Instant waiterCalledAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    private void init() {
        if (id == null || id.isEmpty()) {
            id = "SES_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        }
        if (startTime == null) startTime = Instant.now();
    }
}
