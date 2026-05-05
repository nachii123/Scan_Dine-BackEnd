package com.example.scan_dineCustomer.repo;

import com.example.scan_dineCustomer.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    List<Category> findByRestaurant_Id(String restaurantId);
    Optional<Category> findByIdAndRestaurant_Id(String categoryId, String restaurantId);
    boolean existsByNameAndRestaurant_Id(String name, String restaurantId);
}
