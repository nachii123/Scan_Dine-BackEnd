package com.example.scan_dineCustomer.table.dto;

import com.example.scan_dineCustomer.table.entity.Cart;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CartResponse {
    private String cartId;
    private String sessionId;
    private String restaurantId;
    private String customerId;
    private List<CartItemResponse> items;
    private BigDecimal totalAmount;
    private int totalItems;

    public static CartResponse from(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(CartItemResponse::from)
                .collect(Collectors.toList());

        BigDecimal total = items.stream()
                .map(CartItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CartResponse response = new CartResponse();
        response.setCartId(cart.getId());
        response.setSessionId(cart.getSessionId());
        response.setRestaurantId(cart.getRestaurantId());
        response.setCustomerId(cart.getCustomerId());
        response.setItems(items);
        response.setTotalAmount(total);
        response.setTotalItems(items.stream().mapToInt(CartItemResponse::getQuantity).sum());
        return response;
    }
}
