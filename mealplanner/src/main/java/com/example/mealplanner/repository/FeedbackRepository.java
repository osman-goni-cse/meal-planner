package com.example.mealplanner.repository;

import com.example.mealplanner.model.Feedback;
import com.example.mealplanner.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByDateAndMealPeriodOrderByTimestampAsc(LocalDate date, String mealPeriod);
    List<Feedback> findByUserAndDateAndMealPeriod(User user, LocalDate date, String mealPeriod);

    // Efficiently count feedbacks (comments) for a given dish
    long countByDishId(Long dishId);
    long countByDishIdAndDate(Long dishId, LocalDate date);

    // Queries that include dishes with either comments OR reactions
    @Query(value = "SELECT DISTINCT d.id as dishId, d.name, d.image_url as imageUrl, " +
           "COALESCE(commentCount.count, 0) as commentsCount " +
           "FROM dishes d " +
           "LEFT JOIN (SELECT dish_id, COUNT(*) as count FROM feedback GROUP BY dish_id) commentCount ON d.id = commentCount.dish_id " +
           "LEFT JOIN (SELECT dish_id, COUNT(*) as count FROM dish_reactions GROUP BY dish_id) reactionCount ON d.id = reactionCount.dish_id " +
           "WHERE (commentCount.count > 0 OR reactionCount.count > 0) " +
           "ORDER BY commentsCount DESC", nativeQuery = true)
    List<Object[]> findDishesWithCommentsOrReactions(Pageable pageable);

    @Query(value = "SELECT DISTINCT d.id as dishId, d.name, d.image_url as imageUrl, " +
           "COALESCE(commentCount.count, 0) as commentsCount " +
           "FROM dishes d " +
           "LEFT JOIN (SELECT dish_id, COUNT(*) as count FROM feedback GROUP BY dish_id) commentCount ON d.id = commentCount.dish_id " +
           "LEFT JOIN (SELECT dish_id, COUNT(*) as count FROM dish_reactions GROUP BY dish_id) reactionCount ON d.id = reactionCount.dish_id " +
           "WHERE (commentCount.count > 0 OR reactionCount.count > 0) " +
           "AND LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY commentsCount DESC", nativeQuery = true)
    List<Object[]> findDishesWithCommentsOrReactionsBySearch(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query(value = "SELECT DISTINCT d.id as dishId, d.name, d.image_url as imageUrl, " +
           "COALESCE(commentCount.count, 0) as commentsCount, " +
           "(COALESCE(commentCount.count, 0) + COALESCE(reactionCount.count, 0)) as totalActivity " +
           "FROM dishes d " +
           "LEFT JOIN (SELECT dish_id, COUNT(*) as count FROM feedback GROUP BY dish_id) commentCount ON d.id = commentCount.dish_id " +
           "LEFT JOIN (SELECT dish_id, COUNT(*) as count FROM dish_reactions GROUP BY dish_id) reactionCount ON d.id = reactionCount.dish_id " +
           "WHERE (commentCount.count > 0 OR reactionCount.count > 0) " +
           "ORDER BY totalActivity DESC", nativeQuery = true)
    List<Object[]> findDishesWithCommentsOrReactionsByTotalActivity(Pageable pageable);

    @Query(value = "SELECT DISTINCT d.id as dishId, d.name, d.image_url as imageUrl, " +
           "COALESCE(commentCount.count, 0) as commentsCount, " +
           "(COALESCE(commentCount.count, 0) + COALESCE(reactionCount.count, 0)) as totalActivity " +
           "FROM dishes d " +
           "LEFT JOIN (SELECT dish_id, COUNT(*) as count FROM feedback GROUP BY dish_id) commentCount ON d.id = commentCount.dish_id " +
           "LEFT JOIN (SELECT dish_id, COUNT(*) as count FROM dish_reactions GROUP BY dish_id) reactionCount ON d.id = reactionCount.dish_id " +
           "WHERE (commentCount.count > 0 OR reactionCount.count > 0) " +
           "AND LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY totalActivity DESC", nativeQuery = true)
    List<Object[]> findDishesWithCommentsOrReactionsByTotalActivityAndSearch(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Count queries for dishes with comments OR reactions
    @Query(value = "SELECT COUNT(DISTINCT d.id) FROM dishes d " +
           "LEFT JOIN (SELECT dish_id, COUNT(*) as count FROM feedback GROUP BY dish_id) commentCount ON d.id = commentCount.dish_id " +
           "LEFT JOIN (SELECT dish_id, COUNT(*) as count FROM dish_reactions GROUP BY dish_id) reactionCount ON d.id = reactionCount.dish_id " +
           "WHERE (commentCount.count > 0 OR reactionCount.count > 0)", nativeQuery = true)
    long countDishesWithCommentsOrReactions();

    @Query(value = "SELECT COUNT(DISTINCT d.id) FROM dishes d " +
           "LEFT JOIN (SELECT dish_id, COUNT(*) as count FROM feedback GROUP BY dish_id) commentCount ON d.id = commentCount.dish_id " +
           "LEFT JOIN (SELECT dish_id, COUNT(*) as count FROM dish_reactions GROUP BY dish_id) reactionCount ON d.id = reactionCount.dish_id " +
           "WHERE (commentCount.count > 0 OR reactionCount.count > 0) " +
           "AND LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))", nativeQuery = true)
    long countDishesWithCommentsOrReactionsBySearch(@Param("searchTerm") String searchTerm);
} 