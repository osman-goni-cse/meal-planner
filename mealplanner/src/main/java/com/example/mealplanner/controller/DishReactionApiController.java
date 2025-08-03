package com.example.mealplanner.controller;

import com.example.mealplanner.dto.DishReactionRequest;
import com.example.mealplanner.dto.DishReactionResponse;
import com.example.mealplanner.model.User;
import com.example.mealplanner.model.ReactionType;
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
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

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
        try {
            if (oauth2User == null) {
                return ResponseEntity.status(401).body("User not authenticated");
            }
            String email = oauth2User.getAttribute("email");
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401).body("User not found in database");
            }

            // Handle legacy requests (no reactionType specified)
            if (reactionRequest.getReactionType() == null) {
                dishReactionService.toggleReaction(user.getId(), reactionRequest.getDishId());
                long reactions = dishReactionService.getReactions(reactionRequest.getDishId());
                boolean userReacted = dishReactionService.hasUserReacted(user.getId(), reactionRequest.getDishId());
                DishReactionResponse response = new DishReactionResponse(reactionRequest.getDishId(), reactions, userReacted);
                return ResponseEntity.ok(response);
            }

            // Handle new reaction system
            dishReactionService.addOrUpdateReaction(user.getId(), reactionRequest.getDishId(), reactionRequest.getReactionType());
            
            Map<ReactionType, Long> reactionCounts = dishReactionService.getAllReactionCounts(reactionRequest.getDishId());
            ReactionType userReactionType = dishReactionService.getUserReactionType(user.getId(), reactionRequest.getDishId());
            
            DishReactionResponse response = new DishReactionResponse(reactionRequest.getDishId(), reactionCounts, userReactionType);
            return ResponseEntity.ok(response);
            
        } catch (DataIntegrityViolationException e) {
            // Handle database constraint violations
            String errorMessage = "Database constraint error. The reaction type '" + 
                (reactionRequest.getReactionType() != null ? reactionRequest.getReactionType() : "unknown") + 
                "' is not allowed. Please run the database migration to fix this.";
            return ResponseEntity.status(400).body(Map.of("error", errorMessage));
        } catch (Exception e) {
            // Handle other errors
            String errorMessage = "An error occurred while processing the reaction: " + e.getMessage();
            return ResponseEntity.status(500).body(Map.of("error", errorMessage));
        }
    }
} 