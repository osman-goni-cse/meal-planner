package com.example.mealplanner.service;

import com.example.mealplanner.model.DishReaction;
import com.example.mealplanner.model.Dish;
import com.example.mealplanner.model.User;
import com.example.mealplanner.model.ReactionType;
import com.example.mealplanner.repository.DishReactionRepository;
import com.example.mealplanner.repository.DishRepository;
import com.example.mealplanner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
public class DishReactionService {

    @Autowired
    private DishReactionRepository dishReactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DishRepository dishRepository;

    @Transactional
    public void addOrUpdateReaction(Long userId, Long dishId, ReactionType reactionType, LocalDate date) {
        Optional<DishReaction> existingReaction = dishReactionRepository.findByUserIdAndDishIdAndReactionDate(userId, dishId, date);

        if (existingReaction.isPresent()) {
            DishReaction reaction = existingReaction.get();
            if (reaction.getReactionType() == reactionType) {
                // Same reaction type, remove it (toggle off)
                dishReactionRepository.delete(reaction);
            } else {
                // Different reaction type, update it
                reaction.setReactionType(reactionType);
                dishReactionRepository.save(reaction);
            }
        } else {
            // No existing reaction, create new one
            User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
            Dish dish = dishRepository.findById(dishId).orElseThrow(() -> new RuntimeException("Dish not found"));
            DishReaction newReaction = new DishReaction(user, dish, date, reactionType);
            dishReactionRepository.save(newReaction);
        }
    }

    public long getReactions(Long dishId, LocalDate date) {
        return dishReactionRepository.countByDishIdAndReactionDate(dishId, date);
    }

    public Map<ReactionType, Long> getAllReactionCounts(Long dishId, LocalDate date) {
        Map<ReactionType, Long> counts = new HashMap<>();
        for (ReactionType type : ReactionType.values()) {
            counts.put(type, dishReactionRepository.countByDishIdAndReactionTypeAndReactionDate(dishId, type, date));
        }
        return counts;
    }

    public boolean hasUserReacted(Long userId, Long dishId, LocalDate date) {
        return dishReactionRepository.findByUserIdAndDishIdAndReactionDate(userId, dishId, date).isPresent();
    }

    public ReactionType getUserReactionType(Long userId, Long dishId, LocalDate date) {
        Optional<DishReaction> reaction = dishReactionRepository.findByUserIdAndDishIdAndReactionDate(userId, dishId, date);
        return reaction.map(DishReaction::getReactionType).orElse(null);
    }

    // Overloaded methods for backward compatibility (for general statistics without date)
    public long getReactions(Long dishId) {
        return dishReactionRepository.countByDishId(dishId);
    }

    public Map<ReactionType, Long> getAllReactionCounts(Long dishId) {
        Map<ReactionType, Long> counts = new HashMap<>();
        for (ReactionType type : ReactionType.values()) {
            counts.put(type, dishReactionRepository.countByDishIdAndReactionType(dishId, type));
        }
        return counts;
    }

    
} 