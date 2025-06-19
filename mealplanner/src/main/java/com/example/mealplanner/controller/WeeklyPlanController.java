package com.example.mealplanner.controller;

import com.example.mealplanner.model.Dish;
import com.example.mealplanner.model.MenuTemplateEntry;
import com.example.mealplanner.model.MenuOverride;
import com.example.mealplanner.repository.DishRepository;
import com.example.mealplanner.repository.MenuTemplateEntryRepository;
import com.example.mealplanner.repository.MenuOverrideRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

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

    @GetMapping("/weekly-plan")
    public String weeklyPlan(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        LocalDate today = (date != null) ? date : LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

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

        // For each day/meal, get overrides or template
        Map<LocalDate, Map<String, List<Map<String, Object>>>> weekPlan = new LinkedHashMap<>();
        for (LocalDate d : weekDays) {
            Map<String, List<Map<String, Object>>> meals = new LinkedHashMap<>();
            int dow = d.getDayOfWeek().getValue(); // 1=Monday
            for (String meal : MEAL_PERIODS) {
                String key = d + "_" + meal;
                List<Map<String, Object>> dishList = new ArrayList<>();
                if (overrideMap.containsKey(key)) {
                    for (MenuOverride o : overrideMap.get(key)) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("dish", o.getDish());
                        map.put("overrideId", o.getId());
                        dishList.add(map);
                    }
                } else {
                    for (MenuTemplateEntry t : templateRepo.findByDayOfWeekAndMealPeriodOrderBySortOrder(dow, meal)) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("dish", t.getDish());
                        map.put("overrideId", null);
                        dishList.add(map);
                    }
                }
                meals.put(meal, dishList);
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
                    model.addAttribute("error", "No dish selected");
                    return "<div class='text-red-500'>No dish selected</div>";
                }
                return "redirect:/weekly-plan?date=" + date + "&error=nodish";
            }
            int dow = date.getDayOfWeek().getValue();
            Dish dish = dishRepo.findById(dishId).orElseThrow();
            // Remove any existing template entry for this day/meal/dish (to avoid duplicates)
            List<MenuTemplateEntry> existing = templateRepo.findByDayOfWeekAndMealPeriodOrderBySortOrder(dow, mealPeriod);
            boolean alreadyExists = false;
            for (MenuTemplateEntry entry : existing) {
                if (entry.getDish().getId().equals(dishId)) {
                    alreadyExists = true;
                }
            }
            if (!alreadyExists) {
                MenuTemplateEntry newEntry = new MenuTemplateEntry();
                newEntry.setDayOfWeek(dow);
                newEntry.setMealPeriod(mealPeriod);
                newEntry.setDish(dish);
                newEntry.setSortOrder(existing.size());
                templateRepo.save(newEntry);
                templateRepo.flush();
                logger.info("Saved MenuTemplateEntry: {}", newEntry.getId());
            }

            if ("XMLHttpRequest".equals(requestedWith)) {
                // Build weekPlan for this day only using only MenuTemplateEntry
                Map<LocalDate, Map<String, List<Map<String, Object>>>> weekPlan = new LinkedHashMap<>();
                Map<String, List<Map<String, Object>>> meals = new LinkedHashMap<>();
                for (String meal : MEAL_PERIODS) {
                    List<Map<String, Object>> dishList = new ArrayList<>();
                    for (MenuTemplateEntry t : templateRepo.findByDayOfWeekAndMealPeriodOrderBySortOrder(dow, meal)) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("dish", t.getDish());
                        map.put("overrideId", null);
                        dishList.add(map);
                    }
                    meals.put(meal, dishList);
                }
                weekPlan.put(date, meals);

                model.addAttribute("selectedDate", date);
                model.addAttribute("meal", mealPeriod);
                model.addAttribute("weekPlan", weekPlan);
                model.addAttribute("allDishes", dishRepo.findAll());

                // Correct Thymeleaf fragment call
                return "manage-weekly-plan :: mealCard(selectedDate=${selectedDate}, meal=${meal}, weekPlan=${weekPlan}, allDishes=${allDishes})";
            }

            // Fallback: normal redirect
            return "redirect:/weekly-plan?date=" + date;
        } catch (Exception ex) {
            logger.error("Error adding dish to template plan", ex);
            if ("XMLHttpRequest".equals(requestedWith)) {
                model.addAttribute("error", ex.getMessage());
                return "<div class='text-red-500'>Error: " + ex.getMessage() + "</div>";
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
} 