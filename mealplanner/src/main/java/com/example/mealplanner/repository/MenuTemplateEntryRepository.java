package com.example.mealplanner.repository;

import com.example.mealplanner.model.MenuTemplateEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MenuTemplateEntryRepository extends JpaRepository<MenuTemplateEntry, Long> {
    List<MenuTemplateEntry> findByDayOfWeekAndMealPeriodOrderBySortOrder(int dayOfWeek, String mealPeriod);
} 