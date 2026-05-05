package com.example.scan_dineCustomer.service;

import com.example.scan_dineCustomer.dto.RestaurantOnboardRequest;
import com.example.scan_dineCustomer.entity.Restaurant;
import com.example.scan_dineCustomer.repo.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    private final RestaurantRepository restaurantRepository;

    @Transactional
    public Restaurant onboardRestaurant(RestaurantOnboardRequest req) {
        String slug = StringUtils.hasText(req.getSlug())
                ? slugify(req.getSlug())
                : slugify(req.getName());

        restaurantRepository.findBySlug(slug).ifPresent(r -> {
            throw new RuntimeException("Restaurant with slug '" + slug + "' already exists");
        });

        Restaurant restaurant = new Restaurant();
        restaurant.setName(req.getName());
        restaurant.setSlug(slug);
        restaurant.setPhone(req.getPhone());
        restaurant.setEmail(req.getEmail());
        restaurant.setAddress(req.getAddress());
        restaurant.setCity(req.getCity());
        restaurant.setGstin(req.getGstin());
        restaurant.setLogoUrl(req.getLogoUrl());

        if (StringUtils.hasText(req.getCurrency())) restaurant.setCurrency(req.getCurrency());
        if (StringUtils.hasText(req.getTimezone())) restaurant.setTimezone(req.getTimezone());

        return restaurantRepository.save(restaurant);
    }

    @Transactional
    public Restaurant updateRestaurant(String restaurantId, RestaurantOnboardRequest req) {
        Restaurant restaurant = findRestaurantById(restaurantId);

        if (StringUtils.hasText(req.getName())) restaurant.setName(req.getName());
        if (StringUtils.hasText(req.getPhone())) restaurant.setPhone(req.getPhone());
        if (StringUtils.hasText(req.getAddress())) restaurant.setAddress(req.getAddress());
        if (StringUtils.hasText(req.getLogoUrl())) restaurant.setLogoUrl(req.getLogoUrl());
        if (StringUtils.hasText(req.getGstin())) restaurant.setGstin(req.getGstin());

        return restaurantRepository.save(restaurant);
    }

    @Transactional(readOnly = true)
    public Restaurant findRestaurantById(String id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Optional<Restaurant> findRestaurantBySlug(String slug) {
        return restaurantRepository.findBySlug(slug);
    }

    @Transactional(readOnly = true)
    public List<Restaurant> getAllRestaurant() {
        return restaurantRepository.findAll();
    }

    private String slugify(String input) {
        if (input == null) return "";
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }
}
