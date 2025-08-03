package com.example.mealplanner.dto;

import com.example.mealplanner.model.ReactionType;

public class DishReactionRequest {
    private Long dishId;
    private ReactionType reactionType;

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
} 