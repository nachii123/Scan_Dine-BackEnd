package com.example.scan_dineCustomer.auth.controller;

import com.example.scan_dineCustomer.dto.ApiResponse;
import com.example.scan_dineCustomer.dto.CaptainAuthResponse;
import com.example.scan_dineCustomer.dto.CaptainRegistrationRequest;
import com.example.scan_dineCustomer.dto.LoginRequest;
import com.example.scan_dineCustomer.auth.service.CaptainAuthService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/captain/auth")
@RequiredArgsConstructor
public class CaptainAuthController {

    private final CaptainAuthService captainAuthService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@RequestBody CaptainRegistrationRequest request) {
        if (ObjectUtils.isEmpty(request) || ObjectUtils.isEmpty(request.getName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        try {
            CaptainAuthResponse response = captainAuthService.register(request);
            return ResponseEntity.ok(new ApiResponse<>("success", "Captain registered successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequest request) {
        Optional<CaptainAuthResponse> response = captainAuthService.login(request);
        if (response.isPresent()) {
            return ResponseEntity.ok(new ApiResponse<>("success", "Login successful", response.get()));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>("error", "Invalid credentials", null));
    }
}
