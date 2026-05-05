package com.example.scan_dineCustomer.service;

import com.example.scan_dineCustomer.config.CaptainPrincipal;
import com.example.scan_dineCustomer.entity.Captain;
import com.example.scan_dineCustomer.entity.Customer;
import com.example.scan_dineCustomer.repo.CaptainRepository;
import com.example.scan_dineCustomer.repo.CustomerRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;
    private final CaptainRepository captainRepository;

    public CustomerUserDetailsService(CustomerRepository customerRepository, CaptainRepository captainRepository) {
        this.customerRepository = customerRepository;
        this.captainRepository = captainRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Captain> captainOpt = captainRepository.findByEmail(username);
        if (captainOpt.isPresent()) {
            Captain captain = captainOpt.get();
            return new CaptainPrincipal(captain.getId(), captain.getName(), captain.getRestaurantId(), captain.getEmail(), captain.getPassword());
        }

        Optional<Customer> customerOpt = customerRepository.findByEmail(username);
        if (customerOpt.isEmpty()) {
            // OTP-registered customers have no email; JWT subject is their customer ID
            customerOpt = customerRepository.findById(username);
        }
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            String principal = customer.getEmail() != null ? customer.getEmail() : customer.getId();
            return new User(principal, customer.getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        }

        throw new UsernameNotFoundException("User not found: " + username);
    }
}
