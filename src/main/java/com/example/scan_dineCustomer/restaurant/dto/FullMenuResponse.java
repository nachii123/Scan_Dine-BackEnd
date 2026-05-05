package com.example.scan_dineCustomer.restaurant.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FullMenuResponse {
    private String restaurantId;
    private String restaurantName;
    private List<CategoryWithItems> categories;
}
