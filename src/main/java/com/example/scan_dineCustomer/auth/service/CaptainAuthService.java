package com.example.scan_dineCustomer.auth.service;

import com.example.scan_dineCustomer.dto.CaptainAuthResponse;
import com.example.scan_dineCustomer.dto.CaptainRegistrationRequest;
import com.example.scan_dineCustomer.dto.LoginRequest;
import com.example.scan_dineCustomer.entity.Captain;
import com.example.scan_dineCustomer.repo.CaptainRepository;
import com.example.scan_dineCustomer.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CaptainAuthService {

    private final CaptainRepository captainRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public CaptainAuthResponse register(CaptainRegistrationRequest request) {
        if (captainRepository.findByMobile(request.getMobile()).isPresent()) {
            throw new RuntimeException("Mobile number already registered");
        }
        if (captainRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        Captain captain = new Captain();
        captain.setName(request.getName());
        captain.setMobile(request.getMobile());
        captain.setEmail(request.getEmail());
        captain.setPassword(passwordEncoder.encode(request.getPassword()));
        captain.setRestaurantId(request.getRestaurantId());

        Captain saved = captainRepository.save(captain);
        String token = jwtUtil.generateCaptainToken(saved.getId(), saved.getMobile(), saved.getEmail(), saved.getRestaurantId());
        return new CaptainAuthResponse(saved, token);
    }

    public Optional<CaptainAuthResponse> login(LoginRequest request) {
        Optional<Captain> captainOpt = captainRepository.findByMobile(request.getLoginIdentifier());
        if (captainOpt.isEmpty()) {
            captainOpt = captainRepository.findByEmail(request.getLoginIdentifier());
        }

        if (captainOpt.isPresent()) {
            Captain captain = captainOpt.get();
            if (passwordEncoder.matches(request.getPassword(), captain.getPassword())) {
                String token = jwtUtil.generateCaptainToken(captain.getId(), captain.getMobile(), captain.getEmail(), captain.getRestaurantId());
                return Optional.of(new CaptainAuthResponse(captain, token));
            }
        }

        return Optional.empty();
    }
}
