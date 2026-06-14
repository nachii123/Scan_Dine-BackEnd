package com.example.scan_dineCustomer.customer.controller;

import com.example.scan_dineCustomer.dto.ApiResponse;
import com.example.scan_dineCustomer.dto.CustomerResponse;
import com.example.scan_dineCustomer.auth.service.AuthService;
import com.example.scan_dineCustomer.customer.service.CustomerImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
//@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CustomerController {

    @Autowired
    private AuthService authService;

    @Autowired
    private CustomerImportService customerImportService;

    @GetMapping("/{mobileNumber}")
    public ResponseEntity<?> getCustomerDetails(@PathVariable String mobileNumber) {
        Optional<com.example.scan_dineCustomer.entity.Customer> customerOptional = authService.getCustomerByMobile(mobileNumber);
        if (customerOptional.isPresent()) {
            return ResponseEntity.ok(new ApiResponse<>("success", "Customer details found", CustomerResponse.from(customerOptional.get())));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>("error", "Customer not found", null));
        }
    }

    @PostMapping(value = "/import", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<?>> importCustomers(@RequestParam("file") MultipartFile file) {
        try {
            CustomerImportService.ImportResult result = customerImportService.importCustomers(file);
            return ResponseEntity.ok(new ApiResponse<>("success", "Customer file imported successfully", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("error", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("error", "Failed to import customer file: " + e.getMessage(), null));
        }
    }
}
