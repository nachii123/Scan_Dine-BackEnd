package com.example.scan_dineCustomer.table.dto;

import com.example.scan_dineCustomer.enums.ReservationStatus;
import com.example.scan_dineCustomer.table.entity.TableReservation;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class ReservationResponse {
    private String id;
    private String restaurantId;
    private String tableId;
    private String customerName;
    private String customerMobile;
    private int partySize;
    private Instant reservedFor;
    private ReservationStatus status;
    private String notes;
    private Instant createdAt;

    public static ReservationResponse from(TableReservation r) {
        return ReservationResponse.builder()
                .id(r.getId())
                .restaurantId(r.getRestaurantId())
                .tableId(r.getTableId())
                .customerName(r.getCustomerName())
                .customerMobile(r.getCustomerMobile())
                .partySize(r.getPartySize())
                .reservedFor(r.getReservedFor())
                .status(r.getStatus())
                .notes(r.getNotes())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
