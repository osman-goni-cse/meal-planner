package com.example.mealplanner.repository;

import com.example.mealplanner.model.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
 
public interface DishRepository extends JpaRepository<Dish, Long> {
    List<Dish> findByCategory(String category);
} 