package com.example.scan_dineCustomer.table.repository;

import com.example.scan_dineCustomer.table.entity.OrderRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRatingRepository extends JpaRepository<OrderRating, String> {
    Optional<OrderRating> findByOrderId(String orderId);
    List<OrderRating> findByRestaurantIdOrderByCreatedAtDesc(String restaurantId);
    List<OrderRating> findByCustomerId(String customerId);
}
