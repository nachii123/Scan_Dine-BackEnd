package com.example.scan_dineCustomer.table.dto;

import com.example.scan_dineCustomer.enums.ReservationStatus;
import com.example.scan_dineCustomer.table.entity.TableReservation;
import lombok.Data;
import java.time.Instant;

@Data
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
        ReservationResponse response = new ReservationResponse();
        response.setId(r.getId());
        response.setRestaurantId(r.getRestaurantId());
        response.setTableId(r.getTableId());
        response.setCustomerName(r.getCustomerName());
        response.setCustomerMobile(r.getCustomerMobile());
        response.setPartySize(r.getPartySize());
        response.setReservedFor(r.getReservedFor());
        response.setStatus(r.getStatus());
        response.setNotes(r.getNotes());
        response.setCreatedAt(r.getCreatedAt());
        return response;
    }
}
