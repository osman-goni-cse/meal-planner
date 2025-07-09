package com.example.mealplanner.controller;

import com.example.mealplanner.dto.DishReactionRequest;
import com.example.mealplanner.dto.DishReactionResponse;
import com.example.mealplanner.model.User;
import com.example.mealplanner.service.DishReactionService;
import com.example.mealplanner.service.MealService;
import com.example.mealplanner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/reactions")
public class DishReactionApiController {

    @Autowired
    private DishReactionService dishReactionService;

    @Autowired
    private MealService mealService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> handleReaction(@RequestBody DishReactionRequest reactionRequest, @AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        String email = oauth2User.getAttribute("email");
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body("User not found in database");
        }


        dishReactionService.toggleReaction(user.getId(), reactionRequest.getDishId());

        long reactions = dishReactionService.getReactions(reactionRequest.getDishId());
        boolean userReacted = dishReactionService.hasUserReacted(user.getId(), reactionRequest.getDishId());

        DishReactionResponse response = new DishReactionResponse(reactionRequest.getDishId(), reactions, userReacted);
        return ResponseEntity.ok(response);
    }
} 