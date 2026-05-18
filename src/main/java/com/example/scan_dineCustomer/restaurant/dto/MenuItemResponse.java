package com.example.scan_dineCustomer.restaurant.dto;

import com.example.scan_dineCustomer.entity.MenuItem;
import com.example.scan_dineCustomer.enums.AllergenType;
import com.example.scan_dineCustomer.enums.FoodType;
import com.example.scan_dineCustomer.enums.SpiceLevel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class MenuItemResponse {
    private String id;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private FoodType foodType;
    private SpiceLevel spiceLevel;
    private String imageUrl;
    private int displayOrder;
    private boolean available;
    private String categoryId;
    private Set<String> tags;
    private Set<AllergenType> allergens;

    public static MenuItemResponse from(MenuItem item) {
        MenuItemResponse response = new MenuItemResponse();
        response.setId(item.getId());
        response.setName(item.getName());
        response.setDescription(item.getDescription());
        response.setBasePrice(item.getBasePrice());
        response.setFoodType(item.getFoodType());
        response.setSpiceLevel(item.getSpiceLevel());
        response.setImageUrl(item.getImageUrl());
        response.setDisplayOrder(item.getDisplayOrder());
        response.setAvailable(item.isAvailable());
        response.setCategoryId(item.getCategory().getId());
        response.setTags(item.getTags());
        response.setAllergens(item.getAllergens());
        return response;
    }
}
