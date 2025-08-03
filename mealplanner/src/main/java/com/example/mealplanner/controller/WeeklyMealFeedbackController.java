package com.example.mealplanner.controller;

import com.example.mealplanner.model.Dish;
import com.example.mealplanner.model.Feedback;
import com.example.mealplanner.model.User;
import com.example.mealplanner.repository.FeedbackRepository;
import com.example.mealplanner.repository.UserRepository;
import com.example.mealplanner.service.MealService;
import com.example.mealplanner.service.DishReactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.mealplanner.dto.DishCommentDTO;
import java.util.stream.Collectors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import com.example.mealplanner.model.ReactionType;

@Controller
public class WeeklyMealFeedbackController {
    @Autowired
    private MealService mealService;
    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DishReactionService dishReactionService;

    @GetMapping("/weekly-feedback")
    public String weeklyFeedback(@RequestParam(value = "date", required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                 @RequestParam(value = "dish", required = false) Integer dishIndex,
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

        // Gather all dishes for the selected date (across all meal periods)
        List<String> mealPeriods = Arrays.asList("lunch", "snacks");
        List<Dish> allDishes = new ArrayList<>();
        Map<Long, String> dishToMealPeriod = new HashMap<>();
        for (String mealPeriod : mealPeriods) {
            List<Dish> dishes = mealService.getDishesForMeal(mealPeriod, selectedDate);
            for (Dish d : dishes) {
                allDishes.add(d);
                dishToMealPeriod.put(d.getId(), mealPeriod);
            }
        }

        // Build dish DTOs for the view
        List<Map<String, Object>> dishDTOs = new ArrayList<>();
        for (Dish dish : allDishes) {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", dish.getId());
            dto.put("name", dish.getName());
            dto.put("imageUrl", dish.getImageUrl());
            dto.put("description", dish.getDescription());
            String mealPeriod = dishToMealPeriod.get(dish.getId());
            dto.put("mealPeriod", mealPeriod);
            // Feedbacks for this dish (by date and meal period, and dish id if available)
            List<Feedback> feedbacks = feedbackRepository.findByDateAndMealPeriodOrderByTimestampAsc(selectedDate, mealPeriod);
            List<Map<String, Object>> feedbackList = new ArrayList<>();
            
            for (Feedback f : feedbacks) {
                if (f.getDishId() != null && f.getDishId().equals(dish.getId())) {
                    Map<String, Object> fb = new HashMap<>();
                    fb.put("userName", f.getUserName());
                    fb.put("userAvatarUrl", f.getUserAvatarUrl());
                    fb.put("comment", f.getComment());
                    feedbackList.add(fb);
                    
                }
            }
            dto.put("feedbacks", feedbackList);
            // Use repository method to count all comments for this dish
            int commentsCountTotal = (int) feedbackRepository.countByDishId(dish.getId());
            dto.put("commentsCount", commentsCountTotal);
            // Add reactions and userReacted
            long reactions = dishReactionService.getReactions(dish.getId());
            dto.put("reactions", reactions);
            
            // Add reaction counts for each type
            Map<ReactionType, Long> reactionCounts = dishReactionService.getAllReactionCounts(dish.getId());
            // Convert to String keys for frontend compatibility
            Map<String, Long> reactionCountsString = new HashMap<>();
            for (Map.Entry<ReactionType, Long> entry : reactionCounts.entrySet()) {
                reactionCountsString.put(entry.getKey().name(), entry.getValue());
            }
            dto.put("reactionCounts", reactionCountsString);
            
            boolean userReacted = false;
            String userReactionType = null;
            if (oauth2User != null) {
                String email = oauth2User.getAttribute("email");
                User user = userRepository.findByEmail(email).orElse(null);
                if (user != null) {
                    userReacted = dishReactionService.hasUserReacted(user.getId(), dish.getId());
                    if (userReacted) {
                        userReactionType = dishReactionService.getUserReactionType(user.getId(), dish.getId()).name();
                    }
                }
            }
            dto.put("userReacted", userReacted);
            dto.put("userReactionType", userReactionType);
            dishDTOs.add(dto);
        }

        // Determine selected dish
        int selectedDishIndex = (dishIndex != null && dishIndex >= 0 && dishIndex < dishDTOs.size()) ? dishIndex : 0;
        Map<String, Object> selectedDish = dishDTOs.isEmpty() ? null : dishDTOs.get(selectedDishIndex);
        model.addAttribute("dishes", dishDTOs);
        model.addAttribute("selectedDish", selectedDish);
        model.addAttribute("selectedDishIndex", selectedDishIndex);
        return "weekly-meal-feedback";
    }

    @PostMapping("/weekly-feedback/feedback")
    public String submitFeedback(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                 @RequestParam("dishId") Long dishId,
                                 @RequestParam("comment") String comment,
                                 @AuthenticationPrincipal OAuth2User oauth2User,
                                 RedirectAttributes redirectAttributes) {
        // Find all dishes for the date to determine the dish index
        List<String> mealPeriods = Arrays.asList("lunch", "snacks");
        List<Dish> allDishes = new ArrayList<>();
        for (String mp : mealPeriods) {
            List<Dish> dishes = mealService.getDishesForMeal(mp, date);
            allDishes.addAll(dishes);
        }
        int dishIndex = 0;
        for (int i = 0; i < allDishes.size(); i++) {
            if (allDishes.get(i).getId().equals(dishId)) {
                dishIndex = i;
                break;
            }
        }
        if (comment == null || comment.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Comment cannot be empty.");
            return "redirect:/weekly-feedback?date=" + date + "&dish=" + dishIndex;
        }
        if (oauth2User != null) {
            String email = oauth2User.getAttribute("email");
            User user = userRepository.findByEmail(email).orElse(null);
            String userName = oauth2User.getAttribute("name");
            String userAvatarUrl = oauth2User.getAttribute("picture");
            // Find mealPeriod for this dishId and date
            String mealPeriod = null;
            for (String mp : mealPeriods) {
                List<Dish> dishes = mealService.getDishesForMeal(mp, date);
                for (Dish d : dishes) {
                    if (d.getId().equals(dishId)) {
                        mealPeriod = mp;
                        break;
                    }
                }
                if (mealPeriod != null) break;
            }
            Feedback feedback = new Feedback(user, date, mealPeriod, comment.trim(), LocalDateTime.now(), userName, userAvatarUrl, dishId);
            feedbackRepository.save(feedback);
        }
        return "redirect:/weekly-feedback?date=" + date + "&dish=" + dishIndex;
    }

    @GetMapping("/weekly-feedback/comments")
    @ResponseBody
    public Map<String, Object> getWeeklyFeedbackComments(
        @RequestParam("dishId") Long dishId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size
    ) {
        List<Feedback> all = feedbackRepository.findAll().stream()
            .filter(fb -> dishId.equals(fb.getDishId()))
            .collect(Collectors.toList());
        int from = page * size;
        int to = Math.min(from + size, all.size());
        List<DishCommentDTO> comments = all.subList(from, to).stream().map(fb -> {
            DishCommentDTO dto = new DishCommentDTO();
            dto.setUserName(fb.getUserName() != null ? fb.getUserName() : "Anonymous");
            dto.setAvatarUrl(fb.getUserAvatarUrl() != null ? fb.getUserAvatarUrl() : "/static/default-avatar.png");
            dto.setText(fb.getComment());
            return dto;
        }).collect(Collectors.toList());
        boolean hasMore = to < all.size();
        Map<String, Object> result = new HashMap<>();
        result.put("comments", comments);
        result.put("hasMore", hasMore);
        return result;
    }
} 