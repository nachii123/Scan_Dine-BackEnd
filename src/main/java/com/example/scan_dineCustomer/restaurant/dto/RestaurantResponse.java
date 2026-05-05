package com.example.scan_dineCustomer.restaurant.dto;

import com.example.scan_dineCustomer.entity.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantResponse {
    private String id;
    private String name;
    private String slug;
    private String phone;
    private String email;
    private String address;
    private String city;
    private String currency;
    private String timezone;
    private String gstin;
    private String logoUrl;

    public static RestaurantResponse from(Restaurant restaurant) {
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .slug(restaurant.getSlug())
                .phone(restaurant.getPhone())
                .email(restaurant.getEmail())
                .address(restaurant.getAddress())
                .city(restaurant.getCity())
                .currency(restaurant.getCurrency())
                .timezone(restaurant.getTimezone())
                .gstin(restaurant.getGstin())
                .logoUrl(restaurant.getLogoUrl())
                .build();
    }
}
