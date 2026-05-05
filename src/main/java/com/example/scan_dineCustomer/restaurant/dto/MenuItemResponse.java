package com.example.scan_dineCustomer.restaurant.dto;

import com.example.scan_dineCustomer.entity.MenuItem;
import com.example.scan_dineCustomer.enums.AllergenType;
import com.example.scan_dineCustomer.enums.FoodType;
import com.example.scan_dineCustomer.enums.SpiceLevel;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
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
        return MenuItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .basePrice(item.getBasePrice())
                .foodType(item.getFoodType())
                .spiceLevel(item.getSpiceLevel())
                .imageUrl(item.getImageUrl())
                .displayOrder(item.getDisplayOrder())
                .available(item.isAvailable())
                .categoryId(item.getCategory().getId())
                .tags(item.getTags())
                .allergens(item.getAllergens())
                .build();
    }
}
