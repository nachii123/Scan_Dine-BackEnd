package com.example.scan_dineCustomer.dto;

import lombok.Data;

@Data
public class OtpVerifyRequest {
    private String mobile;
    private String otp;
    private String name;   // optional — only needed for first-time registration
}
