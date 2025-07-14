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
import com.example.mealplanner.model.MenuTemplateEntry;
import com.example.mealplanner.repository.DishRepository;
import com.example.mealplanner.repository.MenuTemplateEntryRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Controller
public class WeeklyPlanController {

    private static final Logger logger = LoggerFactory.getLogger(WeeklyPlanController.class);

    @Autowired
    private MenuTemplateEntryRepository templateRepo;
    @Autowired
    private DishRepository dishRepo;

    private static final List<String> MEAL_PERIODS = Arrays.asList("breakfast", "lunch", "snacks");

    /**
     * Clean up duplicate MenuTemplateEntry records
     */
    private void cleanupDuplicateTemplateEntries() {
        logger.info("Starting cleanup of duplicate MenuTemplateEntry records");
        
        // Get all entries and group by date and meal period
        List<MenuTemplateEntry> allEntries = templateRepo.findAll();
        Map<String, List<MenuTemplateEntry>> entriesByDateAndMeal = allEntries.stream()
            .collect(Collectors.groupingBy(entry -> entry.getDate() + "_" + entry.getMealPeriod()));
        
        // Remove duplicates for each date/meal combination
        for (Map.Entry<String, List<MenuTemplateEntry>> group : entriesByDateAndMeal.entrySet()) {
            List<MenuTemplateEntry> entries = group.getValue();
            if (entries.size() > 1) {
                // Group by dish ID to find duplicates
                Map<Long, List<MenuTemplateEntry>> dishGroups = entries.stream()
                    .collect(Collectors.groupingBy(entry -> entry.getDish().getId()));
                
                // Remove duplicates for each dish (keep only the first one)
                for (Map.Entry<Long, List<MenuTemplateEntry>> dishGroup : dishGroups.entrySet()) {
                    List<MenuTemplateEntry> duplicates = dishGroup.getValue();
                    if (duplicates.size() > 1) {
                        logger.warn("Found {} duplicate entries for dish {} on date={}, mealPeriod={}, removing extras", 
                                   duplicates.size(), duplicates.get(0).getDish().getName(), 
                                   duplicates.get(0).getDate(), duplicates.get(0).getMealPeriod());
                        
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
     * Get unique dishes for a specific date and meal period
     */
    private List<Map<String, Object>> getUniqueDishesForMeal(LocalDate date, String mealPeriod) {
        List<MenuTemplateEntry> entries = templateRepo.findByDateAndMealPeriodOrderBySortOrder(date, mealPeriod);
        Map<Long, MenuTemplateEntry> uniqueDishes = new LinkedHashMap<>();
        
        for (MenuTemplateEntry entry : entries) {
            uniqueDishes.putIfAbsent(entry.getDish().getId(), entry);
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (MenuTemplateEntry entry : uniqueDishes.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("dish", entry.getDish());
            map.put("templateEntryId", entry.getId());
            result.add(map);
        }
        
        return result;
    }

    @GetMapping("/weekly-plan")
    public String weeklyPlan(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model,
            @AuthenticationPrincipal OAuth2User oauth2User) {

        logger.info("Weekly plan accessed by user: {}", oauth2User != null ? oauth2User.getAttribute("email") : "anonymous");

        // Clean up duplicates before fetching data
        cleanupDuplicateTemplateEntries();

        LocalDate today = (date != null) ? date : LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        logger.info("Loading weekly plan for date: {}, week: {} to {}", today, weekStart, weekEnd);

        // Build week days
        List<LocalDate> weekDays = new ArrayList<>();
        for (int i = 0; i < 7; i++) weekDays.add(weekStart.plusDays(i));

        // --- Full month calendar logic for mobile modal ---
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // 0=Sunday
        LocalDate calendarStart = firstOfMonth.minusDays(firstDayOfWeek);
        List<Map<String, Object>> calendarDays = new ArrayList<>();
        for (int i = 0; i < 42; i++) { // 6 weeks x 7 days
            LocalDate day = calendarStart.plusDays(i);
            Map<String, Object> dayMap = new HashMap<>();
            dayMap.put("date", day);
            dayMap.put("dayOfMonth", day.getDayOfMonth());
            dayMap.put("inMonth", day.getMonth().equals(today.getMonth()));
            dayMap.put("selected", day.equals(today));
            calendarDays.add(dayMap);
        }
        model.addAttribute("calendarDays", calendarDays);
        model.addAttribute("calendarMonthYear", today.getMonth().toString().substring(0,1).toUpperCase() + today.getMonth().toString().substring(1).toLowerCase() + " " + today.getYear());
        model.addAttribute("prevMonthDate", today.minusMonths(1).withDayOfMonth(1));
        model.addAttribute("nextMonthDate", today.plusMonths(1).withDayOfMonth(1));
        // --- End full month calendar logic ---

        // Build week plan with deduplication
        Map<LocalDate, Map<String, List<Map<String, Object>>>> weekPlan = new LinkedHashMap<>();
        for (LocalDate d : weekDays) {
            Map<String, List<Map<String, Object>>> meals = new LinkedHashMap<>();
            for (String meal : MEAL_PERIODS) {
                List<Map<String, Object>> dishList = getUniqueDishesForMeal(d, meal);
                meals.put(meal, dishList);
                logger.debug("Dishes for {} {}: {} items", d, meal, dishList.size());
            }
            weekPlan.put(d, meals);
        }
        model.addAttribute("currentPath", "/weekly-plan");
        model.addAttribute("weekStart", weekStart);
        model.addAttribute("weekEnd", weekEnd);
        model.addAttribute("weekDays", weekDays);
        model.addAttribute("selectedDate", today);
        model.addAttribute("weekPlan", weekPlan);
        model.addAttribute("mealPeriods", MEAL_PERIODS);
        model.addAttribute("allDishes", dishRepo.findAll());
        model.addAttribute("pageTitle", "Weekly Meal Planning");
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
            Dish dish = dishRepo.findById(dishId).orElseThrow();
            
            // Check if this dish already exists for this day/meal combination
            List<MenuTemplateEntry> existing = templateRepo.findByDateAndMealPeriodOrderBySortOrder(date, mealPeriod);
            
            // Clean up any existing duplicates for this day/meal/dish combination
            List<MenuTemplateEntry> duplicatesToRemove = new ArrayList<>();
            for (MenuTemplateEntry entry : existing) {
                if (entry.getDish().getId().equals(dishId)) {
                    duplicatesToRemove.add(entry);
                }
            }
            
            // Remove duplicates (keep only the first one)
            if (duplicatesToRemove.size() > 1) {
                logger.warn("Found {} duplicate entries for dish {} on date={}, mealPeriod={}, removing extras", 
                           duplicatesToRemove.size(), dish.getName(), date, mealPeriod);
                for (int i = 1; i < duplicatesToRemove.size(); i++) {
                    templateRepo.delete(duplicatesToRemove.get(i));
                }
                templateRepo.flush();
            }
            
            boolean alreadyExists = duplicatesToRemove.size() > 0;
            
            if (!alreadyExists) {
                MenuTemplateEntry newEntry = new MenuTemplateEntry();
                newEntry.setDate(date);
                newEntry.setMealPeriod(mealPeriod);
                newEntry.setDish(dish);
                newEntry.setSortOrder(existing.size());
                templateRepo.save(newEntry);
                templateRepo.flush();
                logger.info("Saved MenuTemplateEntry: {} for dish: {}", newEntry.getId(), dish.getName());
            } else {
                logger.info("Dish {} already exists for date={}, mealPeriod={}, skipping", dish.getName(), date, mealPeriod);
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
                    List<Map<String, Object>> dishList = getUniqueDishesForMeal(date, meal);
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

    @PostMapping("/weekly-plan/remove-template")
    public String removeDishFromTemplate(
            @RequestParam("dishId") Long dishId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("mealPeriod") String mealPeriod,
            Model model,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        
        try {
            logger.info("Remove dish from template: date={}, mealPeriod={}, dishId={}", date, mealPeriod, dishId);
            
            // Find and delete the template entry
            List<MenuTemplateEntry> entries = templateRepo.findByDateAndMealPeriodOrderBySortOrder(date, mealPeriod);
            for (MenuTemplateEntry entry : entries) {
                if (entry.getDish().getId().equals(dishId)) {
                    templateRepo.delete(entry);
                    templateRepo.flush();
                    logger.info("Deleted MenuTemplateEntry: {} for dish: {}", entry.getId(), entry.getDish().getName());
                    break;
                }
            }

            if ("XMLHttpRequest".equals(requestedWith)) {
                // Build updated weekPlan for AJAX response
                Map<LocalDate, Map<String, List<Map<String, Object>>>> weekPlan = new LinkedHashMap<>();
                Map<String, List<Map<String, Object>>> meals = new LinkedHashMap<>();
                
                for (String meal : MEAL_PERIODS) {
                    List<Map<String, Object>> dishList = getUniqueDishesForMeal(date, meal);
                    meals.put(meal, dishList);
                }
                
                weekPlan.put(date, meals);

                model.addAttribute("selectedDate", date);
                model.addAttribute("meal", mealPeriod);
                model.addAttribute("weekPlan", weekPlan);
                model.addAttribute("allDishes", dishRepo.findAll());
                
                return "manage-weekly-plan :: mealCard(selectedDate=${selectedDate}, meal=${meal}, weekPlan=${weekPlan}, allDishes=${allDishes})";
            }

            return "redirect:/weekly-plan?date=" + date;
        } catch (Exception ex) {
            logger.error("Error removing dish from template", ex);
            if ("XMLHttpRequest".equals(requestedWith)) {
                return handleAjaxError(ex.getMessage());
            }
            return "redirect:/weekly-plan?date=" + date + "&error=exception";
        }
    }

    @ResponseBody
    private String handleAjaxError(String message) {
        return "<div class='text-red-500'>" + message + "</div>";
    }
} 