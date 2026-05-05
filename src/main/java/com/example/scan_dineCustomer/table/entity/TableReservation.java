package com.example.scan_dineCustomer.table.entity;

import com.example.scan_dineCustomer.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "table_reservation", schema = "tables")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TableReservation {

    @Id
    @Column(length = 15)
    private String id;

    @Column(name = "restaurant_id", nullable = false, length = 15)
    private String restaurantId;

    @Column(name = "table_id", length = 15)
    private String tableId;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "customer_mobile", nullable = false, length = 15)
    private String customerMobile;

    @Column(name = "party_size", nullable = false)
    private int partySize;

    @Column(name = "reserved_for", nullable = false)
    private Instant reservedFor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    @Column(length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    private void generateId() {
        if (id == null || id.isEmpty()) {
            id = "RES_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        }
    }
}
