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
    private long reactions;

    @Transient
    private boolean userReacted;

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

    public long getReactions() {
        return reactions;
    }

    public void setReactions(long reactions) {
        this.reactions = reactions;
    }

    public boolean isUserReacted() {
        return userReacted;
    }

    public void setUserReacted(boolean userReacted) {
        this.userReacted = userReacted;
    }
} 