package com.example.scan_dineCustomer.restaurant.controller;

import com.example.scan_dineCustomer.dto.RestaurantOnboardRequest;
import com.example.scan_dineCustomer.entity.Restaurant;
import com.example.scan_dineCustomer.restaurant.dto.FullMenuResponse;
import com.example.scan_dineCustomer.restaurant.dto.MenuCategoryRequest;
import com.example.scan_dineCustomer.restaurant.dto.MenuCategoryResponse;
import com.example.scan_dineCustomer.restaurant.dto.MenuItemRequest;
import com.example.scan_dineCustomer.restaurant.dto.MenuItemResponse;
import com.example.scan_dineCustomer.restaurant.service.MenuService;
import com.example.scan_dineCustomer.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
@Slf4j
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final MenuService menuService;

    @PostMapping("/onboard")
    public ResponseEntity<?> onboardRestaurant(@RequestBody @Valid RestaurantOnboardRequest req) {
        try {
            Restaurant restaurant = restaurantService.onboardRestaurant(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(restaurant);
        } catch (RuntimeException e) {
            log.error("Error onboarding restaurant: {}", e.getMessage());
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to onboard restaurant: " + e.getMessage());
        }
    }

    @PutMapping("/{restaurantId}")
    public ResponseEntity<?> updateRestaurant(
            @PathVariable String restaurantId,
            @RequestBody @Valid RestaurantOnboardRequest req) {
        try {
            return ResponseEntity.ok(restaurantService.updateRestaurant(restaurantId, req));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Restaurant not found with id: " + restaurantId);
        }
    }

    @GetMapping("/byRestaurantId")
    public ResponseEntity<?> getRestaurantById(@RequestParam String restaurantId) {
        try {
            return ResponseEntity.ok(restaurantService.findRestaurantById(restaurantId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Restaurant not found with id: " + restaurantId);
        }
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> getRestaurantBySlug(@PathVariable String slug) {
        return restaurantService.findRestaurantBySlug(slug)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Restaurant not found with slug: " + slug));
    }

    @GetMapping
    public ResponseEntity<?> getAllRestaurants() {
        List<Restaurant> restaurants = restaurantService.getAllRestaurant();
        if (CollectionUtils.isEmpty(restaurants)) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(restaurants);
    }

    // Customer scans QR code → gets full menu for the restaurant + table
    @GetMapping("/{restaurantId}/menu")
    public ResponseEntity<?> getRestaurantMenu(
            @PathVariable String restaurantId,
            @RequestParam(required = false) String tableId) {
        try {
            FullMenuResponse menu = menuService.getFullMenuByRestaurantId(restaurantId);
            return ResponseEntity.ok(menu);
        } catch (RuntimeException e) {
            log.error("Menu fetch failed for restaurant {}: {}", restaurantId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Restaurant not found: " + restaurantId);
        }
    }

    // Get all categories for a restaurant
    @GetMapping("/menu/categories")
    public ResponseEntity<?> getCategoriesByRestaurant(@RequestParam String restaurantId) {
        try {
            List<MenuCategoryResponse> categories = menuService.getCategoriesByRestaurantId(restaurantId);
            if (categories.isEmpty()) return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            return ResponseEntity.ok(categories);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Get all items for a category within a restaurant
    @GetMapping("/menu/items")
    public ResponseEntity<?> getItemsByCategory(
            @RequestParam String restaurantId,
            @RequestParam String categoryId) {
        try {
            List<MenuItemResponse> items = menuService.getItemsByCategoryId(restaurantId, categoryId);
            if (items.isEmpty()) return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            return ResponseEntity.ok(items);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Admin: create menu category (restaurantId in request body)
    @PostMapping("/menu/categories")
    public ResponseEntity<?> createMenuCategory(@RequestBody MenuCategoryRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(MenuCategoryResponse.from(menuService.createMenuCategory(request)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Admin: create menu item (restaurantId + categoryId in request body)
    @PostMapping("/menu/items")
    public ResponseEntity<?> createMenuItem(@RequestBody MenuItemRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(MenuItemResponse.from(menuService.createMenuItem(request)));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
