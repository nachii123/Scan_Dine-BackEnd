package com.example.scan_dineCustomer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantOnboardRequest {
    @NotBlank
    String name;
    String slug;
    String phone;
    @Email
    String email;
    String address;
    String city;
    String currency;
    String timezone;
    String gstin;
    String logoUrl;
}
