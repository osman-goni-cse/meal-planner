package com.example.mealplanner.repository;

import com.example.mealplanner.model.MenuTemplateEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MenuTemplateEntryRepository extends JpaRepository<MenuTemplateEntry, Long> {
    List<MenuTemplateEntry> findByDayOfWeekAndMealPeriodOrderBySortOrder(int dayOfWeek, String mealPeriod);
    List<MenuTemplateEntry> findByDayOfWeek(int dayOfWeek);
} 