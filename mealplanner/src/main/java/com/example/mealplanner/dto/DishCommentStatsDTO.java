package com.example.mealplanner.dto;

public class DishCommentStatsDTO {
    private Long id;
    private String name;
    private String imageUrl;
    private int likes;
    private int commentsCount;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }
    public int getCommentsCount() { return commentsCount; }
    public void setCommentsCount(int commentsCount) { this.commentsCount = commentsCount; }
} 