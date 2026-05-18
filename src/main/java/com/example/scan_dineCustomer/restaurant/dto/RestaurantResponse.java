package com.example.scan_dineCustomer.restaurant.dto;

import com.example.scan_dineCustomer.entity.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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
        RestaurantResponse response = new RestaurantResponse();
        response.setId(restaurant.getId());
        response.setName(restaurant.getName());
        response.setSlug(restaurant.getSlug());
        response.setPhone(restaurant.getPhone());
        response.setEmail(restaurant.getEmail());
        response.setAddress(restaurant.getAddress());
        response.setCity(restaurant.getCity());
        response.setCurrency(restaurant.getCurrency());
        response.setTimezone(restaurant.getTimezone());
        response.setGstin(restaurant.getGstin());
        response.setLogoUrl(restaurant.getLogoUrl());
        return response;
    }
}
