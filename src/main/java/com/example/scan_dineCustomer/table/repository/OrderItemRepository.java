package com.example.scan_dineCustomer.table.repository;

import com.example.scan_dineCustomer.enums.OrderItemStatus;
import com.example.scan_dineCustomer.table.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    List<OrderItem> findByOrderId(String orderId);
    Optional<OrderItem> findByIdAndOrderId(String id, String orderId);
    long countByOrderIdAndStatus(String orderId, OrderItemStatus status);
}
