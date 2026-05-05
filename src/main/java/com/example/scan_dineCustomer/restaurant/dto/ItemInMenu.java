package com.example.scan_dineCustomer.restaurant.dto;

import com.example.scan_dineCustomer.entity.MenuItem;
import com.example.scan_dineCustomer.enums.FoodType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ItemInMenu {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private FoodType foodType;
    private String imageUrl;

    public static ItemInMenu from(MenuItem entity) {
        return ItemInMenu.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getBasePrice())
                .foodType(entity.getFoodType())
                .imageUrl(entity.getImageUrl())
                .build();
    }
}
