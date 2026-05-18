package com.example.scan_dineCustomer.table.repository;

import com.example.scan_dineCustomer.enums.OrderStatus;
import com.example.scan_dineCustomer.table.entity.DineOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<DineOrder, String> {
    DineOrder findDineOrderById(String orderId);
    List<DineOrder> findBySessionId(String sessionId);
    List<DineOrder> findByCustomerIdOrderByCreatedAtDesc(String customerId);
    List<DineOrder> findByCustomerIdAndRestaurantIdOrderByCreatedAtDesc(String customerId, String restaurantId);
    List<DineOrder> findByRestaurantIdAndStatus(String restaurantId, OrderStatus status);
    List<DineOrder> findByRestaurantIdAndStatusIn(String restaurantId, List<OrderStatus> statuses);
    List<DineOrder> findByTableIdAndStatus(String tableId, OrderStatus status);
    long countBySessionIdAndStatus(String sessionId, OrderStatus status);
    List<DineOrder> findByRestaurantIdAndCreatedAtBetweenAndStatusNot(String restaurantId, Instant start, Instant end, OrderStatus status);
}
