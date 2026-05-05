package com.example.scan_dineCustomer.table.repository;

import com.example.scan_dineCustomer.enums.SessionStatus;
import com.example.scan_dineCustomer.table.entity.TableSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableSessionRepository extends JpaRepository<TableSession, String> {
    Optional<TableSession> findByTable_IdAndStatus(String tableId, SessionStatus status);
    List<TableSession> findByRestaurantIdAndStatus(String restaurantId, SessionStatus status);
    boolean existsByTable_IdAndStatus(String tableId, SessionStatus status);
    Optional<TableSession> findByIdAndRestaurantId(String sessionId, String restaurantId);
}
