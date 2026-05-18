package com.example.scan_dineCustomer.table.dto;

import com.example.scan_dineCustomer.table.entity.CartItem;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemResponse {
    private String id;
    private String menuItemId;
    private String menuItemName;
    private String imageUrl;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal totalPrice;
    private String notes;

    public static CartItemResponse from(CartItem item) {
        CartItemResponse response = new CartItemResponse();
        response.setId(item.getId());
        response.setMenuItemId(item.getMenuItemId());
        response.setMenuItemName(item.getMenuItemName());
        response.setImageUrl(item.getImageUrl());
        response.setUnitPrice(item.getUnitPrice());
        response.setQuantity(item.getQuantity());
        response.setTotalPrice(item.getTotalPrice());
        response.setNotes(item.getNotes());
        return response;
    }
}
