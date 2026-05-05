package com.example.scan_dineCustomer.config;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CaptainPrincipal implements UserDetails {

    private final String captainId;
    private final String name;
    private final String restaurantId;
    private final String email;
    private final String password;

    public CaptainPrincipal(String captainId, String name, String restaurantId, String email, String password) {
        this.captainId = captainId;
        this.name = name;
        this.restaurantId = restaurantId;
        this.email = email;
        this.password = password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_CAPTAIN"));
    }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return email; }
}