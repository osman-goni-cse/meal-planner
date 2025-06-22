package com.example.mealplanner.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.mealplanner.model.Dish;
import com.example.mealplanner.model.MenuOverride;
import com.example.mealplanner.model.MenuTemplateEntry;
import com.example.mealplanner.repository.DishRepository;
import com.example.mealplanner.repository.MenuOverrideRepository;
import com.example.mealplanner.repository.MenuTemplateEntryRepository;

@Controller
public class WeeklyPlanController {

    private static final Logger logger = LoggerFactory.getLogger(WeeklyPlanController.class);

    @Autowired
    private MenuTemplateEntryRepository templateRepo;
    @Autowired
    private MenuOverrideRepository overrideRepo;
    @Autowired
    private DishRepository dishRepo;

    private static final List<String> MEAL_PERIODS = Arrays.asList("breakfast", "lunch", "snacks");

    /**
     * Clean up duplicate MenuTemplateEntry records
     */
    private void cleanupDuplicateTemplateEntries() {
        logger.info("Starting cleanup of duplicate MenuTemplateEntry records");
        
        for (int dayOfWeek = 1; dayOfWeek <= 7; dayOfWeek++) {
            for (String mealPeriod : MEAL_PERIODS) {
                List<MenuTemplateEntry> entries = templateRepo.findByDayOfWeekAndMealPeriodOrderBySortOrder(dayOfWeek, mealPeriod);
                
                // Group by dish ID to find duplicates
                Map<Long, List<MenuTemplateEntry>> dishGroups = entries.stream()
                    .collect(Collectors.groupingBy(entry -> entry.getDish().getId()));
                
                // Remove duplicates for each dish (keep only the first one)
                for (Map.Entry<Long, List<MenuTemplateEntry>> group : dishGroups.entrySet()) {
                    List<MenuTemplateEntry> duplicates = group.getValue();
                    if (duplicates.size() > 1) {
                        logger.warn("Found {} duplicate entries for dish {} on dayOfWeek={}, mealPeriod={}, removing extras", 
                                   duplicates.size(), duplicates.get(0).getDish().getName(), dayOfWeek, mealPeriod);
                        
                        // Keep the first one, remove the rest
                        for (int i = 1; i < duplicates.size(); i++) {
                            templateRepo.delete(duplicates.get(i));
                        }
                    }
                }
            }
        }
        templateRepo.flush();
        logger.info("Completed cleanup of duplicate MenuTemplateEntry records");
    }

    /**
     * Get unique dishes for a specific day and meal period
     */
    private List<Map<String, Object>> getUniqueDishesForMeal(int dayOfWeek, String mealPeriod) {
        List<MenuTemplateEntry> entries = templateRepo.findByDayOfWeekAndMealPeriodOrderBySortOrder(dayOfWeek, mealPeriod);
        Map<Long, MenuTemplateEntry> uniqueDishes = new LinkedHashMap<>();
        
        for (MenuTemplateEntry entry : entries) {
            uniqueDishes.putIfAbsent(entry.getDish().getId(), entry);
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (MenuTemplateEntry entry : uniqueDishes.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("dish", entry.getDish());
            map.put("overrideId", null);
            result.add(map);
        }
        
        return result;
    }

    @GetMapping("/weekly-plan")
    public String weeklyPlan(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        // Clean up duplicates before fetching data
        cleanupDuplicateTemplateEntries();

        LocalDate today = (date != null) ? date : LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        logger.info("Loading weekly plan for date: {}, week: {} to {}", today, weekStart, weekEnd);

        // Build week days
        List<LocalDate> weekDays = new ArrayList<>();
        for (int i = 0; i < 7; i++) weekDays.add(weekStart.plusDays(i));

        // Fetch overrides for the week
        List<MenuOverride> overrides = overrideRepo.findByDateBetween(weekStart, weekEnd);
        Map<String, List<MenuOverride>> overrideMap = new HashMap<>();
        for (MenuOverride o : overrides) {
            String key = o.getDate() + "_" + o.getMealPeriod();
            overrideMap.computeIfAbsent(key, k -> new ArrayList<>()).add(o);
        }

        // Build week plan with deduplication
        Map<LocalDate, Map<String, List<Map<String, Object>>>> weekPlan = new LinkedHashMap<>();
        for (LocalDate d : weekDays) {
            Map<String, List<Map<String, Object>>> meals = new LinkedHashMap<>();
            int dow = d.getDayOfWeek().getValue(); // 1=Monday
            
            for (String meal : MEAL_PERIODS) {
                String key = d + "_" + meal;
                List<Map<String, Object>> dishList = new ArrayList<>();
                
                if (overrideMap.containsKey(key)) {
                    // Use overrides
                    for (MenuOverride o : overrideMap.get(key)) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("dish", o.getDish());
                        map.put("overrideId", o.getId());
                        dishList.add(map);
                    }
                } else {
                    // Use template entries with deduplication
                    dishList = getUniqueDishesForMeal(dow, meal);
                }
                
                meals.put(meal, dishList);
                logger.debug("Dishes for {} {}: {} items", d, meal, dishList.size());
            }
            weekPlan.put(d, meals);
        }

        model.addAttribute("weekStart", weekStart);
        model.addAttribute("weekEnd", weekEnd);
        model.addAttribute("weekDays", weekDays);
        model.addAttribute("selectedDate", today);
        model.addAttribute("weekPlan", weekPlan);
        model.addAttribute("mealPeriods", MEAL_PERIODS);
        model.addAttribute("allDishes", dishRepo.findAll());
        return "manage-weekly-plan";
    }

    @PostMapping("/weekly-plan/add")
    public String addDishToPlan(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("mealPeriod") String mealPeriod,
            @RequestParam("dishId") Long dishId,
            Model model,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        try {
            logger.info("Add dish to template: date={}, mealPeriod={}, dishId={}", date, mealPeriod, dishId);
            if (dishId == null) {
                logger.error("dishId is null");
                if ("XMLHttpRequest".equals(requestedWith)) {
                    return handleAjaxError("No dish selected");
                }
                return "redirect:/weekly-plan?date=" + date + "&error=nodish";
            }
            int dow = date.getDayOfWeek().getValue();
            Dish dish = dishRepo.findById(dishId).orElseThrow();
            
            // Check if this dish already exists for this day/meal combination
            List<MenuTemplateEntry> existing = templateRepo.findByDayOfWeekAndMealPeriodOrderBySortOrder(dow, mealPeriod);
            
            // Clean up any existing duplicates for this day/meal/dish combination
            List<MenuTemplateEntry> duplicatesToRemove = new ArrayList<>();
            for (MenuTemplateEntry entry : existing) {
                if (entry.getDish().getId().equals(dishId)) {
                    duplicatesToRemove.add(entry);
                }
            }
            
            // Remove duplicates (keep only the first one)
            if (duplicatesToRemove.size() > 1) {
                logger.warn("Found {} duplicate entries for dish {} on dayOfWeek={}, mealPeriod={}, removing extras", 
                           duplicatesToRemove.size(), dish.getName(), dow, mealPeriod);
                for (int i = 1; i < duplicatesToRemove.size(); i++) {
                    templateRepo.delete(duplicatesToRemove.get(i));
                }
                templateRepo.flush();
            }
            
            boolean alreadyExists = duplicatesToRemove.size() > 0;
            
            if (!alreadyExists) {
                MenuTemplateEntry newEntry = new MenuTemplateEntry();
                newEntry.setDayOfWeek(dow);
                newEntry.setMealPeriod(mealPeriod);
                newEntry.setDish(dish);
                newEntry.setSortOrder(existing.size());
                templateRepo.save(newEntry);
                templateRepo.flush();
                logger.info("Saved MenuTemplateEntry: {} for dish: {}", newEntry.getId(), dish.getName());
            } else {
                logger.info("Dish {} already exists for dayOfWeek={}, mealPeriod={}, skipping", dish.getName(), dow, mealPeriod);
            }

            if ("XMLHttpRequest".equals(requestedWith)) {
                logger.debug("=== AJAX RESPONSE DEBUG ===");
                logger.debug("mealPeriod parameter: {}", mealPeriod);
                logger.debug("mealPeriod is null: {}", mealPeriod == null);
                
                // Validate mealPeriod
                if (mealPeriod == null || mealPeriod.trim().isEmpty()) {
                    logger.error("mealPeriod is null or empty in AJAX request");
                    return handleAjaxError("Invalid meal period");
                }
                
                // Build weekPlan for this day only using the same deduplication method
                Map<LocalDate, Map<String, List<Map<String, Object>>>> weekPlan = new LinkedHashMap<>();
                Map<String, List<Map<String, Object>>> meals = new LinkedHashMap<>();
                
                for (String meal : MEAL_PERIODS) {
                    List<Map<String, Object>> dishList = getUniqueDishesForMeal(dow, meal);
                    meals.put(meal, dishList);
                }
                
                weekPlan.put(date, meals);

                model.addAttribute("selectedDate", date);
                model.addAttribute("meal", mealPeriod);
                model.addAttribute("weekPlan", weekPlan);
                model.addAttribute("allDishes", dishRepo.findAll());
                
                logger.debug("Model attributes set - meal: {}", mealPeriod);
                logger.debug("=== AJAX RESPONSE DEBUG END ===");

                // Correct Thymeleaf fragment call with proper parameter mapping
                return "manage-weekly-plan :: mealCard(selectedDate=${selectedDate}, meal=${meal}, weekPlan=${weekPlan}, allDishes=${allDishes})";
            }

            // Fallback: normal redirect
            return "redirect:/weekly-plan?date=" + date;
        } catch (Exception ex) {
            logger.error("Error adding dish to template plan", ex);
            if ("XMLHttpRequest".equals(requestedWith)) {
                return handleAjaxError(ex.getMessage());
            }
            return "redirect:/weekly-plan?date=" + date + "&error=exception";
        }
    }

    @PostMapping("/weekly-plan/remove")
    public String removeDishFromPlan(@RequestParam("overrideId") Long overrideId,
                                     @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        overrideRepo.deleteById(overrideId);
        return "redirect:/weekly-plan?date=" + date;
    }

    @ResponseBody
    private String handleAjaxError(String message) {
        return "<div class='text-red-500'>" + message + "</div>";
    }
} 