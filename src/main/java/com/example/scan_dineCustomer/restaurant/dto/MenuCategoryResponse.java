package com.example.scan_dineCustomer.restaurant.dto;

import com.example.scan_dineCustomer.entity.Category;
import com.example.scan_dineCustomer.enums.CategoryType;
import lombok.Data;

@Data
public class MenuCategoryResponse {
    private String id;
    private String name;
    private String description;
    private CategoryType type;
    private String imageUrl;
    private int displayOrder;
    private boolean available;
    private String restaurantId;

    public static MenuCategoryResponse from(Category category) {
        MenuCategoryResponse response = new MenuCategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setType(category.getType());
        response.setImageUrl(category.getImageUrl());
        response.setDisplayOrder(category.getDisplayOrder());
        response.setAvailable(category.isAvailable());
        response.setRestaurantId(category.getRestaurant().getId());
        return response;
    }
}
