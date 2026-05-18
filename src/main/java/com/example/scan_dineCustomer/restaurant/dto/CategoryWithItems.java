package com.example.scan_dineCustomer.restaurant.dto;

import com.example.scan_dineCustomer.entity.Category;
import com.example.scan_dineCustomer.entity.MenuItem;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class CategoryWithItems {
    private String id;
    private String name;
    private String imageUrl;
    private List<ItemInMenu> items;

    public static CategoryWithItems from(Category entity) {
        List<ItemInMenu> activeItems = entity.getItems().stream()
                .filter(MenuItem::isActive)
                .filter(MenuItem::isAvailable)
                .map(ItemInMenu::from)
                .collect(Collectors.toList());

        CategoryWithItems response = new CategoryWithItems();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setImageUrl(entity.getImageUrl());
        response.setItems(activeItems);
        return response;
    }
}
