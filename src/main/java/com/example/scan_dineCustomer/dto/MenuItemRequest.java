package com.example.scan_dineCustomer.dto;

import com.example.scan_dineCustomer.enums.FoodType;
import com.example.scan_dineCustomer.enums.SpiceLevel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class MenuItemRequest {
    private String restaurantId;
    private String categoryId;
    private String name;
    private String description;
    private FoodType foodType;
    private BigDecimal basePrice;
    private String imageUrl;
    private boolean available;
    private boolean featured;
    private int displayOrder;
    private int prepTimeMinutes;
    private SpiceLevel spiceLevel;
    private Set<String> tags;
}
