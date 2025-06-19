package com.example.mealplanner.model;

import jakarta.persistence.*;

@Entity
@Table(name = "menu_template_entry")
public class MenuTemplateEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int dayOfWeek; // 1=Monday, ..., 7=Sunday
    private String mealPeriod; // breakfast, lunch, snacks

    @ManyToOne
    @JoinColumn(name = "dish_id")
    private Dish dish;

    private int sortOrder;

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public String getMealPeriod() { return mealPeriod; }
    public void setMealPeriod(String mealPeriod) { this.mealPeriod = mealPeriod; }
    public Dish getDish() { return dish; }
    public void setDish(Dish dish) { this.dish = dish; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
} 