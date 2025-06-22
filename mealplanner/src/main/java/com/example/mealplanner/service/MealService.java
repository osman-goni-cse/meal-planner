package com.example.mealplanner.service;

import com.example.mealplanner.model.Dish;
import com.example.mealplanner.model.MenuOverride;
import com.example.mealplanner.model.MenuTemplateEntry;
import com.example.mealplanner.repository.MenuOverrideRepository;
import com.example.mealplanner.repository.MenuTemplateEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MealService {

    @Autowired
    private MenuOverrideRepository overrideRepo;

    @Autowired
    private MenuTemplateEntryRepository templateRepo;

    /**
     * Determines the current meal period based on the current time.
     * Breakfast: 6:00 AM - 9:59 AM
     * Lunch: 11:00 AM - 1:59 PM
     * Snacks: 2:00 PM - 4:59 PM
     * @return A string representing the current meal period ("breakfast", "lunch", "snacks") or null if no meal is being served.
     */
    public String getCurrentMealPeriod() {
        LocalTime now = LocalTime.now();
        if (!now.isBefore(LocalTime.of(6, 0)) && now.isBefore(LocalTime.of(10, 0))) {
            return "breakfast";
        }
        if (!now.isBefore(LocalTime.of(11, 0)) && now.isBefore(LocalTime.of(14, 0))) {
            return "lunch";
        }
        if (!now.isBefore(LocalTime.of(14, 0)) && now.isBefore(LocalTime.of(17, 0))) {
            return "snacks";
        }
        return null;
    }

    /**
     * Fetches the list of dishes for a given meal period and date.
     * It first checks for overrides and then falls back to the template.
     * @param mealPeriod The meal period ("breakfast", "lunch", "snacks").
     * @param date The date for which to fetch the dishes.
     * @return A list of unique Dish objects.
     */
    public List<Dish> getDishesForMeal(String mealPeriod, LocalDate date) {
        if (mealPeriod == null) {
            return Collections.emptyList();
        }

        int dow = date.getDayOfWeek().getValue();

        // Check for overrides first
        List<MenuOverride> overrides = overrideRepo.findByDateBetween(date, date);
        List<Dish> dishes = overrides.stream()
            .filter(o -> o.getMealPeriod().equalsIgnoreCase(mealPeriod))
            .map(MenuOverride::getDish)
            .distinct()
            .collect(Collectors.toList());

        if (!dishes.isEmpty()) {
            return dishes;
        }

        // Fallback to template, ensuring no duplicates
        return templateRepo.findByDayOfWeekAndMealPeriodOrderBySortOrder(dow, mealPeriod)
            .stream()
            .map(MenuTemplateEntry::getDish)
            .distinct()
            .collect(Collectors.toList());
    }
} 