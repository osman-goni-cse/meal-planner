package com.example.mealplanner.controller;

import com.example.mealplanner.model.Dish;
import com.example.mealplanner.service.MealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private MealService mealService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // --- Current Meal Info ---
        String currentMealPeriod = mealService.getCurrentMealPeriod();
        List<Dish> dishes = mealService.getDishesForMeal(currentMealPeriod, LocalDate.now());
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a"));

        model.addAttribute("pageTitle", "Today's Menu");
        model.addAttribute("currentMealPeriod", currentMealPeriod);
        model.addAttribute("dishes", dishes);
        model.addAttribute("currentTime", currentTime);

        if (currentMealPeriod != null) {
            model.addAttribute("mealInfo", String.format("%s Time - %s", 
                currentMealPeriod.substring(0, 1).toUpperCase() + currentMealPeriod.substring(1), 
                currentTime));
        }

        // --- Weekly Overview Info ---
        LocalDate today = LocalDate.now();
        List<LocalDate> weekDays = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            weekDays.add(today.plusDays(i));
        }
        model.addAttribute("weekDays", weekDays);
        model.addAttribute("today", today);

        // --- Tomorrow's Preview ---
        LocalDate tomorrow = today.plusDays(1);
        String breakfastPreview = mealService.getDishesForMeal("breakfast", tomorrow)
                                             .stream().map(Dish::getName).findFirst().orElse("Not planned");
        String lunchPreview = mealService.getDishesForMeal("lunch", tomorrow)
                                         .stream().map(Dish::getName).findFirst().orElse("Not planned");
        String snacksPreview = mealService.getDishesForMeal("snacks", tomorrow)
                                          .stream().map(Dish::getName).findFirst().orElse("Not planned");

        model.addAttribute("breakfastPreview", breakfastPreview);
        model.addAttribute("lunchPreview", lunchPreview);
        model.addAttribute("snacksPreview", snacksPreview);
        
        String previewTitle;
        if (tomorrow.equals(LocalDate.now().plusDays(1))) {
            previewTitle = "Tomorrow's Preview";
        } else {
            previewTitle = "Preview for " + tomorrow.format(DateTimeFormatter.ofPattern("MMM d"));
        }
        model.addAttribute("previewTitle", previewTitle);
        model.addAttribute("selectedDate", tomorrow);

        return "dashboard";
    }

    @GetMapping("/dashboard/preview")
    public String getMealPreview(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, Model model) {
        String breakfastPreview = mealService.getDishesForMeal("breakfast", date)
                                             .stream().map(Dish::getName).findFirst().orElse("Not planned");
        String lunchPreview = mealService.getDishesForMeal("lunch", date)
                                         .stream().map(Dish::getName).findFirst().orElse("Not planned");
        String snacksPreview = mealService.getDishesForMeal("snacks", date)
                                          .stream().map(Dish::getName).findFirst().orElse("Not planned");

        model.addAttribute("breakfastPreview", breakfastPreview);
        model.addAttribute("lunchPreview", lunchPreview);
        model.addAttribute("snacksPreview", snacksPreview);
        model.addAttribute("selectedDate", date);
        model.addAttribute("today", LocalDate.now());

        String previewTitle;
        if (date.equals(LocalDate.now().plusDays(1))) {
            previewTitle = "Tomorrow's Preview";
        } else {
            previewTitle = "Preview for " + date.format(DateTimeFormatter.ofPattern("MMM d"));
        }
        model.addAttribute("previewTitle", previewTitle);

        return "dashboard :: meal-preview";
    }
} 