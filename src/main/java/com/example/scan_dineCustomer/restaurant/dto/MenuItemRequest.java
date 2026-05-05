package com.example.scan_dineCustomer.restaurant.dto;

import com.example.scan_dineCustomer.enums.AllergenType;
import com.example.scan_dineCustomer.enums.FoodType;
import com.example.scan_dineCustomer.enums.SpiceLevel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Data
public class MenuItemRequest {
    private String restaurantId;
    private String categoryId;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private FoodType foodType;
    private SpiceLevel spiceLevel;
    private int displayOrder;
    private String imageUrl;
    private boolean available = true;
    private Set<AllergenType> allergens = new HashSet<>();
}
