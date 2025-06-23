package com.example.mealplanner.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "daily_votes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "dish_id", "vote_date", "meal_period"})
})
public class DailyVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;

    @Column(name = "vote_date", nullable = false)
    private LocalDate voteDate;

    @Column(name = "meal_period", nullable = false)
    private String mealPeriod;

    @Enumerated(EnumType.STRING)
    @Column(name = "vote_type", nullable = false)
    private VoteType voteType;

    public DailyVote() {}

    public DailyVote(User user, Dish dish, LocalDate voteDate, String mealPeriod, VoteType voteType) {
        this.user = user;
        this.dish = dish;
        this.voteDate = voteDate;
        this.mealPeriod = mealPeriod;
        this.voteType = voteType;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Dish getDish() { return dish; }
    public void setDish(Dish dish) { this.dish = dish; }
    public LocalDate getVoteDate() { return voteDate; }
    public void setVoteDate(LocalDate voteDate) { this.voteDate = voteDate; }
    public String getMealPeriod() { return mealPeriod; }
    public void setMealPeriod(String mealPeriod) { this.mealPeriod = mealPeriod; }
    public VoteType getVoteType() { return voteType; }
    public void setVoteType(VoteType voteType) { this.voteType = voteType; }
} 