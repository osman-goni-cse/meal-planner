package com.example.mealplanner.repository;

import com.example.mealplanner.model.MenuTemplateEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MenuTemplateEntryRepository extends JpaRepository<MenuTemplateEntry, Long> {
    // Date-based queries
    List<MenuTemplateEntry> findByDateAndMealPeriodOrderBySortOrder(LocalDate date, String mealPeriod);
    List<MenuTemplateEntry> findByDate(LocalDate date);
    List<MenuTemplateEntry> findByDateBetween(LocalDate startDate, LocalDate endDate);
} 