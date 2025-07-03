package com.example.mealplanner.dto;

public class DishCommentStatsDTO {
    private Long id;
    private String name;
    private String imageUrl;
    private long reactions;
    private int commentsCount;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public long getReactions() { return reactions; }
    public void setReactions(long reactions) { this.reactions = reactions; }
    public int getCommentsCount() { return commentsCount; }
    public void setCommentsCount(int commentsCount) { this.commentsCount = commentsCount; }
} 