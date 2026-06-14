package com.example.scan_dineCustomer.dto;

import com.example.scan_dineCustomer.entity.Customer;
import lombok.Data;

@Data
public class CustomerResponse {
    private String id;
    private String name;
    private String mobile;
    private String email;

    public static CustomerResponse from(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setName(customer.getName());
        response.setMobile(customer.getMobile());
        response.setEmail(customer.getEmail());
        return response;
    }
}
