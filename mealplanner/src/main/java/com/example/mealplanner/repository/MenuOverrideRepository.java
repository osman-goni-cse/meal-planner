package com.example.mealplanner.repository;

import com.example.mealplanner.model.MenuOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface MenuOverrideRepository extends JpaRepository<MenuOverride, Long> {
    List<MenuOverride> findByDateAndMealPeriodOrderBySortOrder(LocalDate date, String mealPeriod);
    List<MenuOverride> findByDateBetween(LocalDate start, LocalDate end);
} 