package com.example.scan_dineCustomer.dto;

import lombok.Data;

@Data
public class RegistrationRequest {
    private String name;
    private String mobile;
    private String email;
    private String password;
}
