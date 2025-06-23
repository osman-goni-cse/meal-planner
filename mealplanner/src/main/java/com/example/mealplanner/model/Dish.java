package com.example.mealplanner.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "dishes")
public class Dish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    private String description;

    private String imageUrl;

    @ElementCollection
    @CollectionTable(name = "dish_dietary_info", joinColumns = @JoinColumn(name = "dish_id"))
    @Column(name = "dietary_info")
    private List<String> dietaryInfo;

    @Transient
    private long upvotes;

    @Transient
    private long downvotes;

    @Transient
    private VoteType currentUserVote;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public List<String> getDietaryInfo() { return dietaryInfo; }
    public void setDietaryInfo(List<String> dietaryInfo) { this.dietaryInfo = dietaryInfo; }

    public long getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(long upvotes) {
        this.upvotes = upvotes;
    }

    public long getDownvotes() {
        return downvotes;
    }

    public void setDownvotes(long downvotes) {
        this.downvotes = downvotes;
    }

    public VoteType getCurrentUserVote() {
        return currentUserVote;
    }

    public void setCurrentUserVote(VoteType currentUserVote) {
        this.currentUserVote = currentUserVote;
    }
} 