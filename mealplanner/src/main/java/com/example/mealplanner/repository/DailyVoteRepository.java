package com.example.mealplanner.repository;

import com.example.mealplanner.model.DailyVote;
import com.example.mealplanner.model.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyVoteRepository extends JpaRepository<DailyVote, Long> {
    Optional<DailyVote> findByUserIdAndDishIdAndVoteDateAndMealPeriod(Long userId, Long dishId, LocalDate voteDate, String mealPeriod);
    long countByDishIdAndVoteDateAndMealPeriodAndVoteType(Long dishId, LocalDate voteDate, String mealPeriod, VoteType voteType);
} 