package com.example.scan_dineCustomer.entity;

import com.example.scan_dineCustomer.enums.AllergenType;
import com.example.scan_dineCustomer.enums.FoodType;
import com.example.scan_dineCustomer.enums.SpiceLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
    name = "menu_items",
    schema = "restaurants",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_menuitem_restaurant_name", columnNames = {"restaurant_id", "name"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {

    @Id
    @Column(length = 15)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false, referencedColumnName = "id")
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FoodType foodType = FoodType.VEG;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    private String imageUrl;

    private boolean available = true;

    private boolean featured = false;

    private boolean active = true;

    private int displayOrder = 0;

    private int prepTimeMinutes = 15;

    @Enumerated(EnumType.STRING)
    private SpiceLevel spiceLevel;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "item_tags", schema = "restaurants", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "item_allergens", schema = "restaurants", joinColumns = @JoinColumn(name = "item_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "allergen")
    private Set<AllergenType> allergens = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    private void generateId() {
        if (id == null || id.isEmpty()) {
            id = "ITEM_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        }
    }
}
