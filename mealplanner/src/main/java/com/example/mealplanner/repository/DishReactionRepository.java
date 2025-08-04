package com.example.mealplanner.repository;

import com.example.mealplanner.model.DishReaction;
import com.example.mealplanner.model.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DishReactionRepository extends JpaRepository<DishReaction, Long> {
    Optional<DishReaction> findByUserIdAndDishId(Long userId, Long dishId);
    Optional<DishReaction> findByUserIdAndDishIdAndReactionDate(Long userId, Long dishId, LocalDate reactionDate);
    long countByDishIdAndReactionTypeAndReactionDate(Long dishId, ReactionType reactionType, LocalDate reactionDate);
    long countByDishIdAndReactionDate(Long dishId, LocalDate reactionDate);
    long countByDishIdAndReactionType(Long dishId, ReactionType reactionType);
    long countByDishId(Long dishId);
} 