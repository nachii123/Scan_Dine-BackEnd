package com.example.scan_dineCustomer.restaurant.dto;

import com.example.scan_dineCustomer.entity.MenuItem;
import com.example.scan_dineCustomer.enums.FoodType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ItemInMenu {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private FoodType foodType;
    private String imageUrl;

    public static ItemInMenu from(MenuItem entity) {
        ItemInMenu response = new ItemInMenu();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        response.setPrice(entity.getBasePrice());
        response.setFoodType(entity.getFoodType());
        response.setImageUrl(entity.getImageUrl());
        return response;
    }
}
