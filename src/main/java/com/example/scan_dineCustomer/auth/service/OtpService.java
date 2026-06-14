package com.example.scan_dineCustomer.auth.service;

import com.example.scan_dineCustomer.dto.AuthResponse;
import com.example.scan_dineCustomer.dto.OtpSendRequest;
import com.example.scan_dineCustomer.dto.OtpVerifyRequest;
import com.example.scan_dineCustomer.entity.Customer;
import com.example.scan_dineCustomer.repo.CustomerRepository;
import com.example.scan_dineCustomer.service.SmsService;
import com.example.scan_dineCustomer.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final int OTP_EXPIRY_MINUTES = 5;

    private final CustomerRepository customerRepository;
    private final SmsService smsService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    // mobile → OtpEntry
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    public void sendOtp(OtpSendRequest request) {
        String mobile = request.getMobile();
        if (mobile == null || mobile.isBlank()) {
            throw new IllegalArgumentException("Mobile number is required");
        }

        String otp = generateOtp();
        otpStore.put(mobile, new OtpEntry(otp, LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)));
        smsService.sendOtp(mobile, otp);
    }

    public AuthResponse verifyOtpAndLogin(OtpVerifyRequest request) {
        String mobile = request.getMobile();
        String otp    = request.getOtp();

        OtpEntry entry = otpStore.get(mobile);

        if (entry == null) {
            throw new IllegalArgumentException("OTP not sent or expired. Please request a new OTP.");
        }
        if (LocalDateTime.now().isAfter(entry.expiresAt())) {
            otpStore.remove(mobile);
            throw new IllegalArgumentException("OTP has expired. Please request a new OTP.");
        }
        if (!entry.otp().equals(otp)) {
            throw new IllegalArgumentException("Invalid OTP.");
        }

        // OTP valid — consume it
        otpStore.remove(mobile);

        // Find existing customer or create a new one
        Optional<Customer> existing = customerRepository.findByMobile(mobile);
        Customer customer = existing.orElseGet(() -> createCustomer(mobile, request.getName()));

        String token = jwtUtil.generateToken(customer.getId(), customer.getMobile(), customer.getEmail() != null ? customer.getEmail() : customer.getId());
        return new AuthResponse(customer, token);
    }

    private Customer createCustomer(String mobile, String name) {
        Customer customer = new Customer();
        customer.setMobile(mobile);
        customer.setName(name != null && !name.isBlank() ? name : "User_" + mobile.substring(Math.max(0, mobile.length() - 4)));
        customer.setEmail(null);
        // Random password — customer uses OTP to sign in, not password
        customer.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        return customerRepository.save(customer);
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(1_000_000));
    }

    private record OtpEntry(String otp, LocalDateTime expiresAt) {}
}
