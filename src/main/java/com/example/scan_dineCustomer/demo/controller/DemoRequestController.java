package com.example.scan_dineCustomer.demo.controller;

import com.example.scan_dineCustomer.demo.dto.DemoRequestPayload;
import com.example.scan_dineCustomer.demo.dto.DemoRequestResponse;
import com.example.scan_dineCustomer.demo.service.DemoRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/demo-requests")
@RequiredArgsConstructor
public class DemoRequestController {

    private final DemoRequestService demoRequestService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody DemoRequestPayload payload) {
        try {
            DemoRequestResponse response = demoRequestService.create(payload);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}
