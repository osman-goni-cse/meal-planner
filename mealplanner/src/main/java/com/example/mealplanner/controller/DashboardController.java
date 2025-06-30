package com.example.mealplanner.controller;

import com.example.mealplanner.model.CustomUserDetails;
import com.example.mealplanner.model.Dish;
import com.example.mealplanner.model.User;
import com.example.mealplanner.repository.UserRepository;
import com.example.mealplanner.service.MealService;
import com.example.mealplanner.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private MealService mealService;

    @Autowired
    private VoteService voteService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal OAuth2User oauth2User) {
        logger.info("Dashboard accessed by user: {}", oauth2User != null ? oauth2User.getAttribute("email") : "anonymous");
        
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        String currentMealPeriod = mealService.getCurrentMealPeriod(now);

        // Determine top and bottom meal periods
        String topMealPeriod = currentMealPeriod;
        String bottomMealPeriod = null;
        if ("lunch".equalsIgnoreCase(currentMealPeriod)) {
            bottomMealPeriod = "snacks";
        } else if ("snacks".equalsIgnoreCase(currentMealPeriod)) {
            bottomMealPeriod = "lunch";
        } else {
            // fallback: show lunch on top, snacks on bottom
            topMealPeriod = "lunch";
            bottomMealPeriod = "snacks";
        }

        // Helper to get main/side for a meal period
        class MealBlock {
            Dish mainDish;
            List<Dish> sideDishes = new ArrayList<>();
            String imageUrl = "/static/default-main.jpg";
            String name = "";
            String description = "";
            MealBlock(List<Dish> dishes) {
                if (dishes != null && !dishes.isEmpty()) {
                    for (Dish d : dishes) {
                        if (mainDish == null && d.getCategory() != null && d.getCategory().equalsIgnoreCase("Main Course")) {
                            mainDish = d;
                        } else if (d.getCategory() != null && d.getCategory().equalsIgnoreCase("Side Dish")) {
                            sideDishes.add(d);
                        }
                    }
                    if (mainDish == null) {
                        mainDish = dishes.get(0);
                    }
                    // Remove main dish from sideDishes if present
                    sideDishes.remove(mainDish);
                    imageUrl = mainDish.getImageUrl() != null ? mainDish.getImageUrl() : imageUrl;
                    name = mainDish.getName();
                    description = mainDish.getDescription();
                }
            }
        }

        MealBlock topMeal = new MealBlock(mealService.getDishesForMeal(topMealPeriod, today));
        MealBlock bottomMeal = new MealBlock(mealService.getDishesForMeal(bottomMealPeriod, today));

        model.addAttribute("currentMealPeriod", currentMealPeriod);
        model.addAttribute("topMealPeriod", topMealPeriod);
        model.addAttribute("topMealMainDish", topMeal.mainDish);
        model.addAttribute("topMealSideDishes", topMeal.sideDishes);
        model.addAttribute("topMealImageUrl", topMeal.imageUrl);
        model.addAttribute("topMealName", topMeal.name);
        model.addAttribute("topMealDescription", topMeal.description);
        model.addAttribute("bottomMealPeriod", bottomMealPeriod);
        model.addAttribute("bottomMealMainDish", bottomMeal.mainDish);
        model.addAttribute("bottomMealSideDishes", bottomMeal.sideDishes);
        model.addAttribute("bottomMealImageUrl", bottomMeal.imageUrl);
        model.addAttribute("bottomMealName", bottomMeal.name);
        model.addAttribute("bottomMealDescription", bottomMeal.description);

        // Serving time string based on meal period
        String servingTime = "";
        if ("breakfast".equalsIgnoreCase(currentMealPeriod)) {
            servingTime = "7:00 AM – 10:00 AM";
        } else if ("lunch".equalsIgnoreCase(currentMealPeriod)) {
            servingTime = "01:00 PM – 5:00 PM";
        } else if ("snacks".equalsIgnoreCase(currentMealPeriod)) {
            servingTime = "5:00 PM – 8:00 PM";
        } else {
            servingTime = "";
        }
        model.addAttribute("servingTime", servingTime);

        model.addAttribute("today", today);
        model.addAttribute("weekDays", mealService.getWeekDays(today));
        LocalDate tomorrow = today.plusDays(1);
        addMealPreviewsToModel(model, tomorrow, "Tomorrow's Preview");
        return "dashboard";
    }

    @GetMapping("/dashboard/preview")
    public String getMealPreview(@RequestParam("date") String dateString, Model model) {
        LocalDate selectedDate = LocalDate.parse(dateString);
        String previewTitle = "Preview for " + selectedDate.getDayOfWeek().toString().substring(0, 1) + selectedDate.getDayOfWeek().toString().substring(1).toLowerCase();
        addMealPreviewsToModel(model, selectedDate, previewTitle);
        return "dashboard :: meal-preview";
    }

    private void addMealPreviewsToModel(Model model, LocalDate date, String title) {
        Map<String, List<Dish>> meals = mealService.getMealsForDay(date);
        model.addAttribute("previewTitle", title);
        model.addAttribute("breakfastPreview", mealService.formatDishesForPreview(meals.get("breakfast")));
        model.addAttribute("lunchPreview", mealService.formatDishesForPreview(meals.get("lunch")));
        model.addAttribute("snacksPreview", mealService.formatDishesForPreview(meals.get("snacks")));
    }

    private String getMealInfo(LocalTime time, String mealPeriod) {
        if (mealPeriod == null) return "No meal being served right now";
        return mealPeriod.substring(0, 1).toUpperCase() + mealPeriod.substring(1) + " Time - " + time.getHour() + ":" + String.format("%02d", time.getMinute());
    }
} 