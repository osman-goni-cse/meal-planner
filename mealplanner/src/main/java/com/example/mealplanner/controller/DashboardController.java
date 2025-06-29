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
        
        List<Dish> dishes = mealService.getDishesForMeal(currentMealPeriod, today);

        User user = null;
        if (oauth2User != null) {
            String email = oauth2User.getAttribute("email");
            user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                logger.info("User role: {}", user.getRole());
            }
        }

        if (user != null && currentMealPeriod != null) {
            final String finalCurrentMealPeriod = currentMealPeriod;
            final Long userId = user.getId();
            dishes.forEach(dish -> {
                dish.setUpvotes(voteService.getUpvotes(dish.getId(), today, finalCurrentMealPeriod));
                dish.setDownvotes(voteService.getDownvotes(dish.getId(), today, finalCurrentMealPeriod));
                dish.setCurrentUserVote(voteService.getCurrentUserVote(userId, dish.getId(), today, finalCurrentMealPeriod));
            });
        }

        model.addAttribute("currentMealPeriod", currentMealPeriod);
        model.addAttribute("dishes", dishes);
        model.addAttribute("mealInfo", getMealInfo(now, currentMealPeriod));
        
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