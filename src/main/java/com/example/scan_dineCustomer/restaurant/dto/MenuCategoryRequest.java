package com.example.scan_dineCustomer.restaurant.dto;

import com.example.scan_dineCustomer.enums.CategoryType;
import lombok.Data;

@Data
public class MenuCategoryRequest {
    private String restaurantId;
    private String name;
    private String description;
    private CategoryType type;
    private String imageUrl;
    private int displayOrder;
}
