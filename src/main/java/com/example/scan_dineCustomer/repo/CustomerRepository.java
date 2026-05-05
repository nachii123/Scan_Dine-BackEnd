package com.example.scan_dineCustomer.repo;

import com.example.scan_dineCustomer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    Optional<Customer> findByMobile(String mobile);
    Optional<Customer> findByEmail(String email);
}
