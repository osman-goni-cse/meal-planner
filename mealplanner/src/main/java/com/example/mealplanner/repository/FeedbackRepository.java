package com.example.mealplanner.repository;

import com.example.mealplanner.model.Feedback;
import com.example.mealplanner.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByDateAndMealPeriodOrderByTimestampAsc(LocalDate date, String mealPeriod);
    List<Feedback> findByUserAndDateAndMealPeriod(User user, LocalDate date, String mealPeriod);

    @Query("SELECT f.dishId, d.name, d.imageUrl, COUNT(f.id) as commentsCount " +
           "FROM Feedback f JOIN Dish d ON f.dishId = d.id " +
           "GROUP BY f.dishId, d.name, d.imageUrl " +
           "ORDER BY commentsCount DESC")
    List<Object[]> findDishCommentStats(Pageable pageable);
} 