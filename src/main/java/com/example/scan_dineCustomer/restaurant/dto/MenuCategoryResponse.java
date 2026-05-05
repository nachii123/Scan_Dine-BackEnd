package com.example.scan_dineCustomer.restaurant.dto;

import com.example.scan_dineCustomer.entity.Category;
import com.example.scan_dineCustomer.enums.CategoryType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
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
        return MenuCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .type(category.getType())
                .imageUrl(category.getImageUrl())
                .displayOrder(category.getDisplayOrder())
                .available(category.isAvailable())
                .restaurantId(category.getRestaurant().getId())
                .build();
    }
}
