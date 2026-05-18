package com.example.scan_dineCustomer.table.repository;

import com.example.scan_dineCustomer.table.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, String> {
    Optional<Cart> findBySessionIdAndCustomerId(String sessionId, String customerId);
    Optional<Cart> findBySessionIdAndCustomerIdAndRestaurantId(String sessionId, String customerId, String restaurantId);
    Optional<Cart> findByCustomerIdAndRestaurantId(String customerId, String restaurantId);
    Optional<Cart> findBySessionId(String sessionId);
}
