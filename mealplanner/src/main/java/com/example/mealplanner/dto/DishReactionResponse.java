package com.example.mealplanner.dto;

import com.example.mealplanner.model.ReactionType;
import java.util.Map;

public class DishReactionResponse {
    private Long dishId;
    private long reactions; // Legacy field for backward compatibility
    private boolean userReacted; // Legacy field for backward compatibility
    private Map<ReactionType, Long> reactionCounts;
    private ReactionType userReactionType;

    public DishReactionResponse(Long dishId, long reactions, boolean userReacted) {
        this.dishId = dishId;
        this.reactions = reactions;
        this.userReacted = userReacted;
    }

    public DishReactionResponse(Long dishId, Map<ReactionType, Long> reactionCounts, ReactionType userReactionType) {
        this.dishId = dishId;
        this.reactionCounts = reactionCounts;
        this.userReactionType = userReactionType;
        this.userReacted = userReactionType != null;
        // Calculate total reactions for backward compatibility
        this.reactions = reactionCounts.values().stream().mapToLong(Long::longValue).sum();
    }

    public Long getDishId() { return dishId; }
    public void setDishId(Long dishId) { this.dishId = dishId; }
    public long getReactions() { return reactions; }
    public void setReactions(long reactions) { this.reactions = reactions; }
    public boolean isUserReacted() { return userReacted; }
    public void setUserReacted(boolean userReacted) { this.userReacted = userReacted; }
    public Map<ReactionType, Long> getReactionCounts() { return reactionCounts; }
    public void setReactionCounts(Map<ReactionType, Long> reactionCounts) { this.reactionCounts = reactionCounts; }
    public ReactionType getUserReactionType() { return userReactionType; }
    public void setUserReactionType(ReactionType userReactionType) { this.userReactionType = userReactionType; }
} 