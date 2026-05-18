package com.example.scan_dineCustomer.table.repository;

import com.example.scan_dineCustomer.table.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {
}
