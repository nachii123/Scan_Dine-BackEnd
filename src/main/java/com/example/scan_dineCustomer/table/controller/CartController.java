package com.example.scan_dineCustomer.table.controller;

import com.example.scan_dineCustomer.table.dto.AddToCartRequest;
import com.example.scan_dineCustomer.table.dto.CartResponse;
import com.example.scan_dineCustomer.table.dto.UpdateCartItemRequest;
import com.example.scan_dineCustomer.table.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody AddToCartRequest request, Authentication auth) {
        try {
            if (request == null) return badRequest("Request body is required");
            if (!StringUtils.hasText(request.getSessionId())) return badRequest("sessionId is required");
            if (!StringUtils.hasText(request.getRestaurantId())) return badRequest("restaurantId is required");
            if (!StringUtils.hasText(request.getMenuItemId())) return badRequest("menuItemId is required");
            if (request.getQuantity() <= 0) return badRequest("quantity must be greater than 0");
            return ResponseEntity.ok(cartService.addToCart(request, auth));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding to cart: " + e.getMessage());
        }
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<?> getCart(
            @PathVariable String sessionId,
            @RequestParam String restaurantId,
            Authentication auth) {
        try {
            if (!StringUtils.hasText(sessionId)) return badRequest("sessionId is required");
            if (!StringUtils.hasText(restaurantId)) return badRequest("restaurantId is required");
            return ResponseEntity.ok(cartService.getCart(sessionId, restaurantId, auth));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching cart: " + e.getMessage());
        }
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<?> getCartByRestaurant(@PathVariable String restaurantId, Authentication auth) {
        try {
            if (!StringUtils.hasText(restaurantId)) return badRequest("restaurantId is required");
            return ResponseEntity.ok(cartService.getCartByRestaurant(restaurantId, auth));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching cart: " + e.getMessage());
        }
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<?> updateCartItem(
            @PathVariable String cartItemId,
            @RequestBody UpdateCartItemRequest request,
            Authentication auth) {
        try {
            if (!StringUtils.hasText(cartItemId)) return badRequest("cartItemId is required");
            if (request == null) return badRequest("Request body is required");
            return ResponseEntity.ok(cartService.updateCartItem(cartItemId, request, auth));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating cart item: " + e.getMessage());
        }
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<?> removeCartItem(@PathVariable String cartItemId, Authentication auth) {
        try {
            if (!StringUtils.hasText(cartItemId)) return badRequest("cartItemId is required");
            return ResponseEntity.ok(cartService.removeCartItem(cartItemId, auth));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error removing cart item: " + e.getMessage());
        }
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<?> clearCart(
            @PathVariable String sessionId,
            @RequestParam String restaurantId,
            Authentication auth) {
        try {
            if (!StringUtils.hasText(sessionId)) return badRequest("sessionId is required");
            if (!StringUtils.hasText(restaurantId)) return badRequest("restaurantId is required");
            cartService.clearCart(sessionId, restaurantId, auth);
            return ResponseEntity.ok("Cart cleared successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error clearing cart: " + e.getMessage());
        }
    }

    private ResponseEntity<String> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }
}
