package com.example.scan_dineCustomer.table.service;

import com.example.scan_dineCustomer.enums.ReservationStatus;
import com.example.scan_dineCustomer.table.dto.ReservationRequest;
import com.example.scan_dineCustomer.table.dto.ReservationResponse;
import com.example.scan_dineCustomer.table.entity.TableReservation;
import com.example.scan_dineCustomer.table.repository.TableReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final Duration RESERVATION_BUFFER = Duration.ofHours(2);

    private final TableReservationRepository reservationRepository;

    @Transactional("tableTransactionManager")
    public ReservationResponse createReservation(ReservationRequest request) {
        if (request.getReservedFor().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Reservation time must be in the future");
        }
        Instant conflictWindowStart = request.getReservedFor().minus(RESERVATION_BUFFER);
        Instant conflictWindowEnd = request.getReservedFor().plus(RESERVATION_BUFFER);
        boolean conflict = reservationRepository
                .findByRestaurantIdAndReservedForBetweenOrderByReservedFor(
                        request.getRestaurantId(), conflictWindowStart, conflictWindowEnd)
                .stream()
                .anyMatch(existing ->
                        existing.getStatus() == ReservationStatus.CONFIRMED
                                && request.getTableId() != null
                                && request.getTableId().equals(existing.getTableId()));
        if (conflict) {
            throw new IllegalStateException("Table is already reserved around this time");
        }
        TableReservation reservation = new TableReservation();
        reservation.setRestaurantId(request.getRestaurantId());
        reservation.setTableId(request.getTableId());
        reservation.setCustomerName(request.getCustomerName());
        reservation.setCustomerMobile(request.getCustomerMobile());
        reservation.setPartySize(request.getPartySize());
        reservation.setReservedFor(request.getReservedFor());
        reservation.setNotes(request.getNotes());
        reservation.setStatus(ReservationStatus.CONFIRMED);
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Transactional(value = "tableTransactionManager", readOnly = true)
    public List<ReservationResponse> getReservationsForDay(String restaurantId, LocalDate date) {
        ZoneId zone = ZoneId.of("Asia/Kolkata");
        Instant start = date.atStartOfDay(zone).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(zone).toInstant();
        return reservationRepository
                .findByRestaurantIdAndReservedForBetweenOrderByReservedFor(restaurantId, start, end)
                .stream().map(ReservationResponse::from).collect(Collectors.toList());
    }

    @Transactional("tableTransactionManager")
    public ReservationResponse updateStatus(String reservationId, ReservationStatus status) {
        TableReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));
        reservation.setStatus(status);
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Transactional(value = "tableTransactionManager", readOnly = true)
    public List<ReservationResponse> getByMobile(String mobile) {
        return reservationRepository.findByCustomerMobile(mobile)
                .stream().map(ReservationResponse::from).collect(Collectors.toList());
    }
}
