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

@Service
public class DishReactionService {

    @Autowired
    private DishReactionRepository dishReactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DishRepository dishRepository;

    @Transactional
    public void toggleReaction(Long userId, Long dishId) {
        Optional<DishReaction> existingReaction = dishReactionRepository.findByUserIdAndDishId(userId, dishId);

        if (existingReaction.isPresent()) {
            dishReactionRepository.delete(existingReaction.get()); // Remove reaction
        } else {
            User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
            Dish dish = dishRepository.findById(dishId).orElseThrow(() -> new RuntimeException("Dish not found"));
            DishReaction newReaction = new DishReaction(user, dish, LocalDate.now(), ReactionType.HEART);
            dishReactionRepository.save(newReaction); // Add reaction
        }
    }

    public long getReactions(Long dishId) {
        return dishReactionRepository.countByDishIdAndReactionType(dishId, ReactionType.HEART);
    }

    public boolean hasUserReacted(Long userId, Long dishId) {
        return dishReactionRepository.findByUserIdAndDishId(userId, dishId).isPresent();
    }
} 