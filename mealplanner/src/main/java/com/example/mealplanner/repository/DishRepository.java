package com.example.mealplanner.repository;

import com.example.mealplanner.model.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
public interface DishRepository extends JpaRepository<Dish, Long> {
    List<Dish> findByCategory(String category);
    List<Dish> findByMealPeriod(String mealPeriod);
    @Query(value = "SELECT * FROM dishes d WHERE " +
              "(:search IS NULL OR LOWER(d.name) LIKE LOWER('%' || :search || '%')) AND " +
              "(:mealPeriod IS NULL OR d.meal_period = :mealPeriod) AND " +
              "(:category IS NULL OR d.category = :category) " +
              "ORDER BY d.created_date DESC", 
       nativeQuery = true)
    Page<Dish> findByFilters(@Param("search") String search, 
                        @Param("mealPeriod") String mealPeriod, 
                        @Param("category") String category, 
                        Pageable pageable);
} 