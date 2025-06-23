package com.example.mealplanner.model;

public class VoteRequest {
    private Long dishId;
    private VoteType voteType;

    // Getters and Setters
    public Long getDishId() {
        return dishId;
    }

    public void setDishId(Long dishId) {
        this.dishId = dishId;
    }

    public VoteType getVoteType() {
        return voteType;
    }

    public void setVoteType(VoteType voteType) {
        this.voteType = voteType;
    }
} 