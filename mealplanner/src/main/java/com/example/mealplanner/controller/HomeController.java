package com.example.mealplanner.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.mealplanner.model.User;
import com.example.mealplanner.repository.UserRepository;
import com.example.mealplanner.service.UserOnboardingService;
import com.example.mealplanner.service.MealService;
import com.example.mealplanner.model.Dish;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {
    
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserOnboardingService userOnboardingService;

    @Autowired
    private MealService mealService;

    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal OAuth2User oauth2User) {
        logger.info("Home page accessed by user: {}", oauth2User != null ? oauth2User.getAttribute("email") : "anonymous");
        
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        String currentMealPeriod = mealService.getCurrentMealPeriod(now);

        // Add time-based greeting
        model.addAttribute("greeting", getTimeBasedGreeting(now));

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
        String servingTime = "01:00 PM – 5:00 PM"; // Default to lunch time
         if ("snacks".equalsIgnoreCase(currentMealPeriod)) {
            servingTime = "5:00 PM – 8:00 PM";
        } 
        model.addAttribute("servingTime", servingTime);

        model.addAttribute("today", today);
        model.addAttribute("weekDays", mealService.getWeekDays(today));
        model.addAttribute("currentPath", "/");
        model.addAttribute("pageTitle", "Today's Menu");
        
        return "dashboard";
    }

    private String getTimeBasedGreeting(LocalTime now) {
        int hour = now.getHour();
        if (hour < 12) {
            return "Good Morning";
        } else if (hour < 18) {
            return "Good Afternoon";
        } else {
            return "Good Evening";
        }
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("pageTitle", "Access Denied");
        return "access-denied";
    }
    
    @GetMapping("/test-auth")
    @ResponseBody
    public String testAuth(@AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User == null) {
            return "Not authenticated";
        }
        
        String email = oauth2User.getAttribute("email");
        String authorities = oauth2User.getAuthorities().toString();
        
        logger.info("Test auth - Email: {}, Authorities: {}", email, authorities);
        
        return String.format("Authenticated as: %s<br>Authorities: %s", email, authorities);
    }
    
    @GetMapping("/debug-user")
    @ResponseBody
    public String debugUser(@AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User == null) {
            return "Not authenticated";
        }
        
        String email = oauth2User.getAttribute("email");
        
        // Use the onboarding service to handle user creation/retrieval
        User user = userOnboardingService.onboardUser(email);
        
        return String.format("User processed through onboarding:<br>Email: %s<br>Role: %s<br>ID: %s", 
                           user.getEmail(), user.getRole(), user.getId());
    }
} 