package com.example.scan_dineCustomer.table.entity;

import com.example.scan_dineCustomer.enums.TableStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "restaurant_table",
    schema = "tables",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_table_restaurant_number", columnNames = {"restaurant_id", "table_number"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantTable {

    @Id
    @Column(length = 15)
    private String id;

    @Column(name = "restaurant_id", nullable = false, length = 15)
    private String restaurantId;

    @Column(name = "table_number", nullable = false, length = 10)
    private String tableNumber;

    @Column(nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TableStatus status = TableStatus.AVAILABLE;

    @Column(name = "qr_code", length = 100)
    private String qrCode;

    @Column(length = 50)
    private String floor;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    private void init() {
        if (id == null || id.isEmpty()) {
            id = "TBL_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        }
        // qrCode encodes restaurantId + tableId so the frontend can parse the QR scan
        if (qrCode == null || qrCode.isEmpty()) {
            qrCode = restaurantId + "|" + id;
        }
    }
}
