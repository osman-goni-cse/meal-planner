package com.example.mealplanner.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "dish_reactions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "dish_id"})
})
public class DishReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Dish dish;

    @Column(name = "reaction_date", nullable = false)
    private LocalDate reactionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false)
    private ReactionType reactionType;

    public DishReaction() {}

    public DishReaction(User user, Dish dish, LocalDate reactionDate, ReactionType reactionType) {
        this.user = user;
        this.dish = dish;
        this.reactionDate = reactionDate;
        this.reactionType = reactionType;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Dish getDish() { return dish; }
    public void setDish(Dish dish) { this.dish = dish; }
    public LocalDate getReactionDate() { return reactionDate; }
    public void setReactionDate(LocalDate reactionDate) { this.reactionDate = reactionDate; }
    public ReactionType getReactionType() { return reactionType; }
    public void setReactionType(ReactionType reactionType) { this.reactionType = reactionType; }
} 