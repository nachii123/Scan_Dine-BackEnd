package com.example.scan_dineCustomer.dto;

import com.example.scan_dineCustomer.entity.Captain;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaptainAuthResponse {
    private Captain captain;
    private String token;
}
