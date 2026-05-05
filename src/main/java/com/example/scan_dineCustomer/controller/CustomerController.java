package com.example.scan_dineCustomer.controller;

import com.example.scan_dineCustomer.dto.ApiResponse;
import com.example.scan_dineCustomer.entity.Customer;
import com.example.scan_dineCustomer.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
//@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CustomerController {

    @Autowired
    private AuthService authService;

    @GetMapping("/{mobileNumber}")
    public ResponseEntity<?> getCustomerDetails(@PathVariable String mobileNumber) {
        Optional<Customer> customerOptional = authService.getCustomerByMobile(mobileNumber);
        if (customerOptional.isPresent()) {
            // In a real app, you'd use a DTO to avoid exposing the password field
            customerOptional.get().setPassword(null); // Never expose password
            return ResponseEntity.ok(new ApiResponse<>("success", "Customer details found", customerOptional.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>("error", "Customer not found", null));
        }
    }
}
