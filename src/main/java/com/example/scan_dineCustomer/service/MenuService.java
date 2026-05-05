package com.example.scan_dineCustomer.service;

import com.example.scan_dineCustomer.entity.Category;
import com.example.scan_dineCustomer.entity.MenuItem;
import com.example.scan_dineCustomer.entity.Restaurant;
import com.example.scan_dineCustomer.repo.CategoryRepository;
import com.example.scan_dineCustomer.repo.MenuItemRepository;
import com.example.scan_dineCustomer.restaurant.dto.CategoryWithItems;
import com.example.scan_dineCustomer.restaurant.dto.FullMenuResponse;
import com.example.scan_dineCustomer.restaurant.dto.MenuCategoryRequest;
import com.example.scan_dineCustomer.restaurant.dto.MenuCategoryResponse;
import com.example.scan_dineCustomer.restaurant.dto.MenuItemRequest;
import com.example.scan_dineCustomer.restaurant.dto.MenuItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
    private final RestaurantService restaurantService;

    @Transactional
    public Category createMenuCategory(MenuCategoryRequest request) {
        Restaurant restaurant = restaurantService.findRestaurantById(request.getRestaurantId());

        Category category = new Category();
        category.setName(request.getName());
        category.setRestaurant(restaurant);
        if (request.getDescription() != null) category.setDescription(request.getDescription());
        if (request.getType() != null) category.setType(request.getType());
        if (request.getImageUrl() != null) category.setImageUrl(request.getImageUrl());
        category.setDisplayOrder(request.getDisplayOrder());

        return categoryRepository.save(category);
    }

    @Transactional
    public MenuItem createMenuItem(MenuItemRequest request) {
        Restaurant restaurant = restaurantService.findRestaurantById(request.getRestaurantId());
        Category category = categoryRepository
                .findByIdAndRestaurant_Id(request.getCategoryId(), request.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Category not found for this restaurant"));

        if (menuItemRepository.existsByNameAndRestaurant_Id(request.getName(), request.getRestaurantId())) {
            throw new IllegalStateException("Item '" + request.getName() + "' already exists in this restaurant.");
        }

        MenuItem item = new MenuItem();
        item.setRestaurant(restaurant);
        item.setCategory(category);
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setBasePrice(request.getBasePrice());
        item.setFoodType(request.getFoodType());
        item.setSpiceLevel(request.getSpiceLevel());
        item.setDisplayOrder(request.getDisplayOrder());
        item.setImageUrl(request.getImageUrl());
        item.setAvailable(request.isAvailable());
        if (request.getAllergens() != null) item.setAllergens(request.getAllergens());

        return menuItemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public List<MenuCategoryResponse> getCategoriesByRestaurantId(String restaurantId) {
        restaurantService.findRestaurantById(restaurantId); // validates restaurant exists
        return categoryRepository.findByRestaurant_Id(restaurantId)
                .stream()
                .map(MenuCategoryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getItemsByCategoryId(String restaurantId, String categoryId) {
        categoryRepository.findByIdAndRestaurant_Id(categoryId, restaurantId)
                .orElseThrow(() -> new RuntimeException("Category not found for this restaurant"));
        return menuItemRepository.findByCategory_Id(categoryId)
                .stream()
                .map(MenuItemResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public MenuItem toggleItemAvailability(String restaurantId, String itemId, boolean available) {
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found: " + itemId));
        if (!item.getRestaurant().getId().equals(restaurantId)) {
            throw new IllegalArgumentException("Item does not belong to this restaurant");
        }
        item.setAvailable(available);
        return menuItemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public FullMenuResponse getFullMenuByRestaurantId(String restaurantId) {
        Restaurant restaurant = restaurantService.findRestaurantById(restaurantId);
        List<Category> categories = categoryRepository.findByRestaurant_Id(restaurantId);

        List<CategoryWithItems> categoryDTOs = categories.stream()
                .filter(Category::isActive)
                .filter(Category::isAvailable)
                .map(CategoryWithItems::from)
                .collect(Collectors.toList());

        return FullMenuResponse.builder()
                .restaurantId(restaurant.getId())
                .restaurantName(restaurant.getName())
                .categories(categoryDTOs)
                .build();
    }
}
