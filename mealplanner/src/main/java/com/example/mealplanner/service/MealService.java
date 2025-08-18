package com.example.mealplanner.service;

import com.example.mealplanner.model.Dish;
import com.example.mealplanner.model.MenuTemplateEntry;
import com.example.mealplanner.repository.MenuTemplateEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MealService {

    @Autowired
    private MenuTemplateEntryRepository templateRepo;


    public String getCurrentMealPeriod(LocalTime time) {
        
        if (time.isBefore(LocalTime.of(16, 00))) {
            return "lunch";
        }
        return "snacks";
    }

    /**
     * Fetches the list of dishes for a given meal period and date.
     * @param mealPeriod The meal period ("breakfast", "lunch", "snacks").
     * @param date The date for which to fetch the dishes.
     * @return A list of unique Dish objects.
     */
    public List<Dish> getDishesForMeal(String mealPeriod, LocalDate date) {
        if (mealPeriod == null) {
            return Collections.emptyList();
        }

        // Get dishes from template entries
        return templateRepo.findByDateAndMealPeriodOrderBySortOrder(date, mealPeriod)
            .stream()
            .map(MenuTemplateEntry::getDish)
            .distinct()
            .collect(Collectors.toList());
    }

    public Map<String, List<Dish>> getMealsForDay(LocalDate date) {
        // Get all template entries for the date
        List<MenuTemplateEntry> entries = templateRepo.findByDate(date);
        return entries.stream()
                .collect(Collectors.groupingBy(
                        MenuTemplateEntry::getMealPeriod,
                        Collectors.mapping(MenuTemplateEntry::getDish, Collectors.toList())
                ));
    }

    public List<LocalDate> getWeekDays(LocalDate today) {
        LocalDate startOfWeek = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.SUNDAY));
        return java.util.stream.Stream.iterate(startOfWeek, d -> d.plusDays(1))
                .limit(7)
                .collect(Collectors.toList());
    }

    public String formatDishesForPreview(List<Dish> dishes) {
        if (dishes == null || dishes.isEmpty()) {
            return "Not planned";
        }
        return dishes.stream().map(Dish::getName).collect(Collectors.joining(", "));
    }
} 