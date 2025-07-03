package com.example.mealplanner.dto;

public class DishReactionResponse {
    private Long dishId;
    private long reactions;
    private boolean userReacted;

    public DishReactionResponse(Long dishId, long reactions, boolean userReacted) {
        this.dishId = dishId;
        this.reactions = reactions;
        this.userReacted = userReacted;
    }

    public Long getDishId() { return dishId; }
    public void setDishId(Long dishId) { this.dishId = dishId; }
    public long getReactions() { return reactions; }
    public void setReactions(long reactions) { this.reactions = reactions; }
    public boolean isUserReacted() { return userReacted; }
    public void setUserReacted(boolean userReacted) { this.userReacted = userReacted; }
} 