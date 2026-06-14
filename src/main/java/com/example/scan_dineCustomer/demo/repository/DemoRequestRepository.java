package com.example.scan_dineCustomer.demo.repository;

import com.example.scan_dineCustomer.demo.entity.DemoRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DemoRequestRepository extends JpaRepository<DemoRequest, String> {
}
