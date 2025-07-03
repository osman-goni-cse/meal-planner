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
    long countByDishIdAndReactionType(Long dishId, ReactionType reactionType);
} 