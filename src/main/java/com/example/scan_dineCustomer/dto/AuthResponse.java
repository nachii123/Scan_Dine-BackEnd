package com.example.scan_dineCustomer.dto;

import com.example.scan_dineCustomer.entity.Customer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private Customer customer;
    private String token;
}
