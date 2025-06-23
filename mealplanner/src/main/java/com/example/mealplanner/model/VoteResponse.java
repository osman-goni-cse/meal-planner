package com.example.mealplanner.model;

public class VoteResponse {
    private Long dishId;
    private long upvotes;
    private long downvotes;
    private VoteType currentUserVote;

    public VoteResponse(Long dishId, long upvotes, long downvotes, VoteType currentUserVote) {
        this.dishId = dishId;
        this.upvotes = upvotes;
        this.downvotes = downvotes;
        this.currentUserVote = currentUserVote;
    }

    // Getters and Setters
    public Long getDishId() { return dishId; }
    public void setDishId(Long dishId) { this.dishId = dishId; }
    public long getUpvotes() { return upvotes; }
    public void setUpvotes(long upvotes) { this.upvotes = upvotes; }
    public long getDownvotes() { return downvotes; }
    public void setDownvotes(long downvotes) { this.downvotes = downvotes; }
    public VoteType getCurrentUserVote() { return currentUserVote; }
    public void setCurrentUserVote(VoteType currentUserVote) { this.currentUserVote = currentUserVote; }
} 