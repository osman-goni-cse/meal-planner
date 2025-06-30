package com.example.mealplanner.controller;

import com.example.mealplanner.model.Dish;
import com.example.mealplanner.model.Feedback;
import com.example.mealplanner.model.User;
import com.example.mealplanner.repository.FeedbackRepository;
import com.example.mealplanner.repository.UserRepository;
import com.example.mealplanner.service.MealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class WeeklyMealFeedbackController {
    @Autowired
    private MealService mealService;
    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/weekly-feedback")
    public String weeklyFeedback(@RequestParam(value = "date", required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                 Model model,
                                 @AuthenticationPrincipal OAuth2User oauth2User) {
        LocalDate selectedDate = (date != null) ? date : LocalDate.now();
        model.addAttribute("selectedDate", selectedDate);

        // Calendar logic
        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // 0=Sunday
        LocalDate calendarStart = firstOfMonth.minusDays(firstDayOfWeek);
        List<Map<String, Object>> calendarDays = new ArrayList<>();
        for (int i = 0; i < 42; i++) { // 6 weeks x 7 days
            LocalDate day = calendarStart.plusDays(i);
            Map<String, Object> dayMap = new HashMap<>();
            dayMap.put("date", day);
            dayMap.put("dayOfMonth", day.getDayOfMonth());
            dayMap.put("inMonth", day.getMonth().equals(selectedDate.getMonth()));
            dayMap.put("selected", day.equals(selectedDate));
            calendarDays.add(dayMap);
        }
        model.addAttribute("calendarDays", calendarDays);
        model.addAttribute("calendarMonthYear", selectedDate.getMonth().toString().substring(0,1).toUpperCase() + selectedDate.getMonth().toString().substring(1).toLowerCase() + " " + selectedDate.getYear());
        model.addAttribute("prevMonthDate", selectedDate.minusMonths(1).withDayOfMonth(1));
        model.addAttribute("nextMonthDate", selectedDate.plusMonths(1).withDayOfMonth(1));

        // Meal periods to display (e.g., lunch, snacks)
        List<String> mealPeriods = Arrays.asList("lunch", "snacks");
        List<Map<String, Object>> mealBlocks = new ArrayList<>();
        for (String mealPeriod : mealPeriods) {
            Map<String, Object> mealBlock = new HashMap<>();
            mealBlock.put("mealPeriod", mealPeriod);
            mealBlock.put("mealPeriodDisplay", mealPeriod.substring(0, 1).toUpperCase() + mealPeriod.substring(1));
            List<Dish> dishes = mealService.getDishesForMeal(mealPeriod, selectedDate);
            Dish mainDish = dishes.stream().filter(d -> "Main Course".equalsIgnoreCase(d.getCategory())).findFirst().orElse(dishes.isEmpty() ? null : dishes.get(0));
            mealBlock.put("mainDishName", mainDish != null ? mainDish.getName() : "");
            mealBlock.put("mainDishImageUrl", mainDish != null ? mainDish.getImageUrl() : "");
            mealBlock.put("mainDishUrl", mainDish != null ? ("/dishes/" + mainDish.getId()) : null);
            mealBlock.put("sideDishes", dishes.stream().filter(d -> !d.equals(mainDish)).toList());
            // Feedbacks
            List<Feedback> feedbacks = feedbackRepository.findByDateAndMealPeriodOrderByTimestampAsc(selectedDate, mealPeriod);
            List<Map<String, Object>> feedbackList = new ArrayList<>();
            for (Feedback f : feedbacks) {
                Map<String, Object> fb = new HashMap<>();
                fb.put("userName", f.getUserName());
                fb.put("userAvatarUrl", f.getUserAvatarUrl());
                fb.put("comment", f.getComment());
                feedbackList.add(fb);
            }
            mealBlock.put("feedbacks", feedbackList);
            mealBlock.put("hasMoreFeedbacks", false); // Implement pagination if needed
            mealBlocks.add(mealBlock);
        }
        model.addAttribute("mealBlocks", mealBlocks);
        return "weekly-meal-feedback";
    }

    @PostMapping("/weekly-feedback/feedback")
    public String submitFeedback(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                 @RequestParam("mealPeriod") String mealPeriod,
                                 @RequestParam("comment") String comment,
                                 @AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User != null) {
            String email = oauth2User.getAttribute("email");
            User user = userRepository.findByEmail(email).orElse(null);
            String userName = oauth2User.getAttribute("name");
            String userAvatarUrl = oauth2User.getAttribute("picture");
            if (user != null && comment != null && !comment.trim().isEmpty()) {
                Feedback feedback = new Feedback(user, date, mealPeriod, comment.trim(), LocalDateTime.now(), userName, userAvatarUrl);
                feedbackRepository.save(feedback);
            }
        }
        return "redirect:/weekly-feedback?date=" + date;
    }
} 