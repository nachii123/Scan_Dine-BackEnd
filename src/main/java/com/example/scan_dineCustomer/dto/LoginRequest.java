package com.example.scan_dineCustomer.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String loginIdentifier; // Can be mobile or email
    private String password;
}
