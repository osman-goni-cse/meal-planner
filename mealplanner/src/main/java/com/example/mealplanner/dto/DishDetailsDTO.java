package com.example.mealplanner.dto;

import java.util.List;

public class DishDetailsDTO {
    private String name;
    private String description;
    private String imageUrl;
    private List<DishCommentDTO> comments;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public List<DishCommentDTO> getComments() { return comments; }
    public void setComments(List<DishCommentDTO> comments) { this.comments = comments; }
} 