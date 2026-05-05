package com.example.scan_dineCustomer.service;

import com.example.scan_dineCustomer.dto.AuthResponse;
import com.example.scan_dineCustomer.dto.LoginRequest;
import com.example.scan_dineCustomer.dto.RegistrationRequest;
import com.example.scan_dineCustomer.entity.Customer;
import com.example.scan_dineCustomer.repo.CustomerRepository;
import com.example.scan_dineCustomer.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final CustomerRepository customerRepository;


    private final PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Customer register(RegistrationRequest registrationRequest) {
        if (customerRepository.findByMobile(registrationRequest.getMobile()).isPresent()) {
            throw new RuntimeException("Mobile number already registered");
        }

        if (customerRepository.findByEmail(registrationRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        Customer customer = new Customer();
        customer.setName(registrationRequest.getName());
        customer.setMobile(registrationRequest.getMobile());
        customer.setEmail(registrationRequest.getEmail());
        customer.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));

        return customerRepository.save(customer);
    }

    public Optional<AuthResponse> login(LoginRequest loginRequest) {
        Optional<Customer> customerOptional = customerRepository.findByMobile(loginRequest.getLoginIdentifier());
        if (customerOptional.isEmpty()) {
            customerOptional = customerRepository.findByEmail(loginRequest.getLoginIdentifier());
        }

        if (customerOptional.isPresent()) {
            Customer customer = customerOptional.get();
            if (passwordEncoder.matches(loginRequest.getPassword(), customer.getPassword())) {
                String token = jwtUtil.generateToken(customer.getId(), customer.getMobile(), customer.getEmail());
                AuthResponse authResponse = new AuthResponse(customer, token);
                return Optional.of(authResponse);
            }
        }

        return Optional.empty();
    }

    public Optional<Customer> getCustomerByMobile(String mobileNumber) {
        return customerRepository.findByMobile(mobileNumber);
    }
}
