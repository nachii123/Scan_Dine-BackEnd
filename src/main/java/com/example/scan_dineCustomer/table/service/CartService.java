package com.example.scan_dineCustomer.table.service;

import com.example.scan_dineCustomer.entity.MenuItem;
import com.example.scan_dineCustomer.repo.MenuItemRepository;
import com.example.scan_dineCustomer.table.dto.AddToCartRequest;
import com.example.scan_dineCustomer.table.dto.CartResponse;
import com.example.scan_dineCustomer.table.dto.UpdateCartItemRequest;
import com.example.scan_dineCustomer.table.entity.Cart;
import com.example.scan_dineCustomer.table.entity.CartItem;
import com.example.scan_dineCustomer.table.repository.CartItemRepository;
import com.example.scan_dineCustomer.table.repository.CartRepository;
import com.example.scan_dineCustomer.table.repository.TableSessionRepository;
import com.example.scan_dineCustomer.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final TableSessionRepository sessionRepository;
    private final JwtUtil jwtUtil;

    @Transactional("tableTransactionManager")
    public CartResponse addToCart(AddToCartRequest request, Authentication auth) {
        String customerId = extractCustomerId(auth);
        assertSessionOwnership(request.getSessionId(), request.getRestaurantId(), customerId);

        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + request.getMenuItemId()));

        if (!menuItem.isAvailable()) {
            throw new IllegalArgumentException("Menu item is currently unavailable: " + menuItem.getName());
        }

        // Guard: menu item must belong to the requested restaurant
        if (!menuItem.getRestaurant().getId().equals(request.getRestaurantId())) {
            throw new IllegalArgumentException("Menu item does not belong to this restaurant");
        }

        Cart cart = cartRepository.findBySessionIdAndCustomerIdAndRestaurantId(
                request.getSessionId(), customerId, request.getRestaurantId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setSessionId(request.getSessionId());
                    newCart.setRestaurantId(request.getRestaurantId());
                    newCart.setCustomerId(customerId);
                    return cartRepository.save(newCart);
                });

        // If item already in cart, increase quantity
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getMenuItemId().equals(request.getMenuItemId()))
                .findFirst();

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            if (request.getNotes() != null) item.setNotes(request.getNotes());
            cartItemRepository.save(item);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setMenuItemId(menuItem.getId());
            item.setMenuItemName(menuItem.getName());
            item.setImageUrl(menuItem.getImageUrl());
            item.setUnitPrice(menuItem.getBasePrice());
            item.setQuantity(request.getQuantity());
            item.setNotes(request.getNotes());
            cart.getItems().add(cartItemRepository.save(item));
        }

        return CartResponse.from(cart);
    }

    @Transactional(value = "tableTransactionManager", readOnly = true)
    public CartResponse getCart(String sessionId, String restaurantId, Authentication auth) {
        String customerId = extractCustomerId(auth);
        assertSessionOwnership(sessionId, restaurantId, customerId);
        Cart cart = cartRepository.findBySessionIdAndCustomerIdAndRestaurantId(sessionId, customerId, restaurantId)
                .orElseGet(() -> emptyCart(sessionId, restaurantId, customerId));
        return CartResponse.from(cart);
    }

    @Transactional(value = "tableTransactionManager", readOnly = true)
    public CartResponse getCartByRestaurant(String restaurantId, Authentication auth) {
        String customerId = extractCustomerId(auth);
        Cart cart = cartRepository.findByCustomerIdAndRestaurantId(customerId, restaurantId)
                .orElseGet(() -> emptyCart(null, restaurantId, customerId));
        return CartResponse.from(cart);
    }

    @Transactional("tableTransactionManager")
    public CartResponse updateCartItem(String cartItemId, UpdateCartItemRequest request, Authentication auth) {
        String customerId = extractCustomerId(auth);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + cartItemId));

        assertCartOwnership(item.getCart().getSessionId(), item.getCart().getRestaurantId(), customerId);

        if (request.getQuantity() <= 0) {
            Cart cart = item.getCart();
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
            return CartResponse.from(cartRepository.findById(cart.getId()).orElse(cart));
        }

        item.setQuantity(request.getQuantity());
        if (request.getNotes() != null) item.setNotes(request.getNotes());
        cartItemRepository.save(item);

        return CartResponse.from(item.getCart());
    }

    @Transactional("tableTransactionManager")
    public CartResponse removeCartItem(String cartItemId, Authentication auth) {
        String customerId = extractCustomerId(auth);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + cartItemId));

        assertCartOwnership(item.getCart().getSessionId(), item.getCart().getRestaurantId(), customerId);

        Cart cart = item.getCart();
        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        return CartResponse.from(cartRepository.findById(cart.getId()).orElse(cart));
    }

    @Transactional("tableTransactionManager")
    public void clearCart(String sessionId, String restaurantId, Authentication auth) {
        String customerId = extractCustomerId(auth);
        assertSessionOwnership(sessionId, restaurantId, customerId);
        cartRepository.findBySessionIdAndCustomerIdAndRestaurantId(sessionId, customerId, restaurantId)
                .ifPresent(cartRepository::delete);
    }

    private Cart emptyCart(String sessionId, String restaurantId, String customerId) {
        Cart empty = new Cart();
        empty.setSessionId(sessionId != null ? sessionId : "");
        empty.setRestaurantId(restaurantId);
        empty.setCustomerId(customerId);
        return empty;
    }

    private String extractCustomerId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("Authentication required");
        }
        String token = auth.getCredentials() != null ? auth.getCredentials().toString() : null;
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Authentication token missing");
        }
        return jwtUtil.extractCustomerId(token);
    }

    private void assertSessionOwnership(String sessionId, String restaurantId, String customerId) {
        var session = sessionRepository.findByIdAndRestaurantId(sessionId, restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        if (!customerId.equals(session.getCustomerId())) {
            throw new AccessDeniedException("Session is owned by another customer");
        }
    }

    private void assertCartOwnership(String sessionId, String restaurantId, String customerId) {
        assertSessionOwnership(sessionId, restaurantId, customerId);
    }
}
