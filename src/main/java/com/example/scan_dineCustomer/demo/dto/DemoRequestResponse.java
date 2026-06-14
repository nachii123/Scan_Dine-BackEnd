package com.example.scan_dineCustomer.demo.dto;

import com.example.scan_dineCustomer.demo.entity.DemoRequest;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DemoRequestResponse {
    private String id;
    private String name;
    private String mobile;
    private String email;
    private LocalDateTime createdAt;

    public static DemoRequestResponse from(DemoRequest request) {
        DemoRequestResponse response = new DemoRequestResponse();
        response.setId(request.getId());
        response.setName(request.getName());
        response.setMobile(request.getMobile());
        response.setEmail(request.getEmail());
        response.setCreatedAt(request.getCreatedAt());
        return response;
    }
}
