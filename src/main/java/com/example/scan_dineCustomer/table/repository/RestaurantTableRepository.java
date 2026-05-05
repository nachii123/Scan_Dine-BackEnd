package com.example.scan_dineCustomer.table.repository;

import com.example.scan_dineCustomer.enums.TableStatus;
import com.example.scan_dineCustomer.table.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, String> {
    List<RestaurantTable> findByRestaurantId(String restaurantId);
    List<RestaurantTable> findByRestaurantIdAndStatus(String restaurantId, TableStatus status);
    Optional<RestaurantTable> findByIdAndRestaurantId(String id, String restaurantId);
    boolean existsByRestaurantIdAndTableNumber(String restaurantId, String tableNumber);
}
