package com.example.scan_dineCustomer.auth.controller;

import com.example.scan_dineCustomer.dto.ApiResponse;
import com.example.scan_dineCustomer.dto.AuthResponse;
import com.example.scan_dineCustomer.dto.LoginRequest;
import com.example.scan_dineCustomer.dto.OtpSendRequest;
import com.example.scan_dineCustomer.dto.OtpVerifyRequest;
import com.example.scan_dineCustomer.dto.RegistrationRequest;
import com.example.scan_dineCustomer.entity.Customer;
import com.example.scan_dineCustomer.auth.service.AuthService;
import com.example.scan_dineCustomer.auth.service.OtpService;
import com.example.scan_dineCustomer.util.JwtUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> registerCustomer(@RequestBody RegistrationRequest registrationRequest) {
        if(ObjectUtils.isEmpty(registrationRequest) || ObjectUtils.isEmpty(registrationRequest.getName())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        try {
            Customer customer = authService.register(registrationRequest);
            String token = jwtUtil.generateToken(customer.getId(), customer.getMobile(), customer.getEmail());
            AuthResponse authResponse = new AuthResponse(customer, token);
            return ResponseEntity.ok(new ApiResponse<>("success", "Customer registered successfully", authResponse));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> loginCustomer(@RequestBody LoginRequest loginRequest) {
        Optional<AuthResponse> authResponseOptional = authService.login(loginRequest);
        if (authResponseOptional.isPresent()) {
            return ResponseEntity.ok(new ApiResponse<>("success", "Login successful", authResponseOptional.get()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>("error", "Invalid credentials", null));
        }
    }

    // ─── OTP Login / Registration ─────────────────────────────────────────────────

    @PostMapping("/otp/send")
    public ResponseEntity<ApiResponse<?>> sendOtp(@RequestBody OtpSendRequest request) {
        if (ObjectUtils.isEmpty(request) || ObjectUtils.isEmpty(request.getMobile())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("error", "Mobile number is required", null));
        }
        try {
            otpService.sendOtp(request);
            return ResponseEntity.ok(new ApiResponse<>("success", "OTP sent to " + request.getMobile(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<?>> verifyOtp(@RequestBody OtpVerifyRequest request) {
        if (ObjectUtils.isEmpty(request) || ObjectUtils.isEmpty(request.getMobile()) || ObjectUtils.isEmpty(request.getOtp())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("error", "Mobile and OTP are required", null));
        }
        try {
            AuthResponse authResponse = otpService.verifyOtpAndLogin(request);
            return ResponseEntity.ok(new ApiResponse<>("success", "OTP verified successfully", authResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("error", e.getMessage(), null));
        }
    }
}
