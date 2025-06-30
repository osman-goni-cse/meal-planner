package com.example.mealplanner.repository;

import com.example.mealplanner.model.Feedback;
import com.example.mealplanner.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByDateAndMealPeriodOrderByTimestampAsc(LocalDate date, String mealPeriod);
    List<Feedback> findByUserAndDateAndMealPeriod(User user, LocalDate date, String mealPeriod);
} 