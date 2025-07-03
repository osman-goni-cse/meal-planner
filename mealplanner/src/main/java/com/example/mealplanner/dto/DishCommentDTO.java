package com.example.mealplanner.dto;

public class DishCommentDTO {
    private String userName;
    private String avatarUrl;
    private String text;

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
} 