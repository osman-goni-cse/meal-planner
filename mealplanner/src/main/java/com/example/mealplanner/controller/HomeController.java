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
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.net.URL;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Controller
public class HomeController {
    
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserOnboardingService userOnboardingService;

    @Autowired
    private MealService mealService;

    // In-memory cache for profile images (url -> (image bytes, timestamp))
    private static final ConcurrentHashMap<String, CachedImage> profileImageCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = TimeUnit.MINUTES.toMillis(30); // 30 min
    private static class CachedImage {
        byte[] data;
        long timestamp;
        String contentType;
        CachedImage(byte[] data, String contentType) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
            this.contentType = contentType;
        }
    }

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
                        } else if (d.getCategory() != null ) {
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
        
        // Build all dishes for the day (across all meal periods, as in WeeklyMealFeedbackController)
        List<String> mealPeriods = Arrays.asList("lunch", "snacks");
        List<Dish> allDishes = new ArrayList<>();
        for (String mp : mealPeriods) {
            List<Dish> dishes = mealService.getDishesForMeal(mp, today);
            allDishes.addAll(dishes);
        }
        // Build a map from dishId to index
        Map<Long, Integer> dishIdToIndex = new HashMap<>();
        for (int i = 0; i < allDishes.size(); i++) {
            dishIdToIndex.put(allDishes.get(i).getId(), i);
        }
        model.addAttribute("dishIdToIndex", dishIdToIndex);

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

    @RequestMapping("/user/profile-image")
    @ResponseBody
    public ResponseEntity<byte[]> proxyProfileImage(@AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String imageUrl = (String) oauth2User.getAttribute("picture");
        if (imageUrl == null || imageUrl.isEmpty()) {
            return defaultAvatar();
        }
        try {
            // Check cache
            CachedImage cached = profileImageCache.get(imageUrl);
            if (cached != null && (System.currentTimeMillis() - cached.timestamp) < CACHE_TTL_MS) {
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(cached.contentType))
                    .body(cached.data);
            }
            // Fetch from Google
            URL url = new URL(imageUrl);
            try (InputStream in = url.openStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                String contentType = guessContentType(imageUrl);
                byte[] buffer = new byte[4096];
                int n;
                while ((n = in.read(buffer)) != -1) {
                    out.write(buffer, 0, n);
                }
                byte[] imageBytes = out.toByteArray();
                profileImageCache.put(imageUrl, new CachedImage(imageBytes, contentType));
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageBytes);
            }
        } catch (Exception e) {
            logger.warn("Failed to fetch Google profile image: {}", e.getMessage());
            return defaultAvatar();
        }
    }

    private ResponseEntity<byte[]> defaultAvatar() {
        try {
            ClassPathResource imgFile = new ClassPathResource("static/default-avatar.png");
            byte[] bytes = StreamUtils.copyToByteArray(imgFile.getInputStream());
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(bytes);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    private String guessContentType(String url) {
        if (url.endsWith(".jpg") || url.endsWith(".jpeg")) return "image/jpeg";
        if (url.endsWith(".png")) return "image/png";
        if (url.endsWith(".gif")) return "image/gif";
        return "image/jpeg";
    }

    @GetMapping("/food-committee")
    public String foodCommittee(Model model) {
        List<User> admins = userRepository.findByRole("ADMIN");
        List<Map<String, String>> committee = new ArrayList<>();
        for (User user : admins) {
            Map<String, String> member = new HashMap<>();
            member.put("name", user.getEmail().split("@")[0]); // Use email prefix as name if no name field
            member.put("role", "Committee Chair");
            member.put("email", user.getEmail());
            member.put("phone", "+1 234 567 8901"); // Placeholder, replace if you have phone field
            String imageUrl = user.getProfileImageUrl();
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = "https://randomuser.me/api/portraits/men/32.jpg"; // Fallback placeholder
            }
            member.put("image", imageUrl);
            member.put("bio", "Passionate about food quality and student satisfaction. Reach out for any menu suggestions or dietary needs!");
            committee.add(member);
        }
        model.addAttribute("committee", committee);
        return "food-committee";
    }
} 