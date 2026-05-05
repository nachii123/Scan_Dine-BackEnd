package com.example.scan_dineCustomer.repo;

import com.example.scan_dineCustomer.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, String> {
    List<MenuItem> findByCategory_Id(String categoryId);
    boolean existsByNameAndRestaurant_Id(String name, String restaurantId);
}
