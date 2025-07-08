package com.example.mealplanner.dto;

import java.util.List;

public class DishDetailsDTO {
    private String name;
    private String description;
    private String imageUrl;
    private List<DishCommentDTO> comments;
    private long reactions;
    private int commentsCount;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public List<DishCommentDTO> getComments() { return comments; }
    public void setComments(List<DishCommentDTO> comments) { this.comments = comments; }
    public long getReactions() { return reactions; }
    public void setReactions(long reactions) { this.reactions = reactions; }
    public int getCommentsCount() { return commentsCount; }
    public void setCommentsCount(int commentsCount) { this.commentsCount = commentsCount; }
} 