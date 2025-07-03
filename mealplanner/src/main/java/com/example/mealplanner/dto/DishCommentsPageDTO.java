package com.example.mealplanner.dto;

import java.util.List;

public class DishCommentsPageDTO {
    private List<DishCommentDTO> comments;
    private boolean hasMore;

    public List<DishCommentDTO> getComments() { return comments; }
    public void setComments(List<DishCommentDTO> comments) { this.comments = comments; }
    public boolean isHasMore() { return hasMore; }
    public void setHasMore(boolean hasMore) { this.hasMore = hasMore; }
} 