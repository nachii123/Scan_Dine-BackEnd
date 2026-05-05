package com.example.scan_dineCustomer.table.repository;

import com.example.scan_dineCustomer.enums.ReservationStatus;
import com.example.scan_dineCustomer.table.entity.TableReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;

@Repository
public interface TableReservationRepository extends JpaRepository<TableReservation, String> {
    List<TableReservation> findByRestaurantIdAndReservedForBetweenOrderByReservedFor(
            String restaurantId, Instant start, Instant end);
    List<TableReservation> findByRestaurantIdAndStatus(String restaurantId, ReservationStatus status);
    List<TableReservation> findByCustomerMobile(String customerMobile);
}
