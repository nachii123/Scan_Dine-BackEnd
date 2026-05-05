package com.example.scan_dineCustomer.repo;

import com.example.scan_dineCustomer.entity.Captain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CaptainRepository extends JpaRepository<Captain, String> {
    Optional<Captain> findByEmail(String email);
    Optional<Captain> findByMobile(String mobile);
}