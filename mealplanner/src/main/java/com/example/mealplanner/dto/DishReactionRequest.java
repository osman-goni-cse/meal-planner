package com.example.mealplanner.dto;

import com.example.mealplanner.model.ReactionType;

public class DishReactionRequest {
    private Long dishId;
    private ReactionType reactionType;
    private java.time.LocalDate date;

    public Long getDishId() {
        return dishId;
    }

    public void setDishId(Long dishId) {
        this.dishId = dishId;
    }

    public ReactionType getReactionType() {
        return reactionType;
    }

    public void setReactionType(ReactionType reactionType) {
        this.reactionType = reactionType;
    }

    public java.time.LocalDate getDate() {
        return date;
    }

    public void setDate(java.time.LocalDate date) {
        this.date = date;
    }
} 