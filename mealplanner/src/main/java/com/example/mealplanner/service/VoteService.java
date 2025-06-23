package com.example.mealplanner.service;

import com.example.mealplanner.model.DailyVote;
import com.example.mealplanner.model.Dish;
import com.example.mealplanner.model.User;
import com.example.mealplanner.model.VoteType;
import com.example.mealplanner.repository.DailyVoteRepository;
import com.example.mealplanner.repository.DishRepository;
import com.example.mealplanner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class VoteService {

    @Autowired
    private DailyVoteRepository dailyVoteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DishRepository dishRepository;

    @Transactional
    public void vote(Long userId, Long dishId, VoteType voteType, LocalDate voteDate, String mealPeriod) {
        Optional<DailyVote> existingVote = dailyVoteRepository.findByUserIdAndDishIdAndVoteDateAndMealPeriod(userId, dishId, voteDate, mealPeriod);

        if (existingVote.isPresent()) {
            DailyVote vote = existingVote.get();
            if (vote.getVoteType() == voteType) {
                dailyVoteRepository.delete(vote);
            } else {
                vote.setVoteType(voteType);
                dailyVoteRepository.save(vote);
            }
        } else {
            User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
            Dish dish = dishRepository.findById(dishId).orElseThrow(() -> new RuntimeException("Dish not found"));
            DailyVote newVote = new DailyVote(user, dish, voteDate, mealPeriod, voteType);
            dailyVoteRepository.save(newVote);
        }
    }

    public long getUpvotes(Long dishId, LocalDate voteDate, String mealPeriod) {
        return dailyVoteRepository.countByDishIdAndVoteDateAndMealPeriodAndVoteType(dishId, voteDate, mealPeriod, VoteType.UPVOTE);
    }

    public long getDownvotes(Long dishId, LocalDate voteDate, String mealPeriod) {
        return dailyVoteRepository.countByDishIdAndVoteDateAndMealPeriodAndVoteType(dishId, voteDate, mealPeriod, VoteType.DOWNVOTE);
    }

    public VoteType getCurrentUserVote(Long userId, Long dishId, LocalDate voteDate, String mealPeriod) {
        return dailyVoteRepository.findByUserIdAndDishIdAndVoteDateAndMealPeriod(userId, dishId, voteDate, mealPeriod)
                .map(DailyVote::getVoteType)
                .orElse(null);
    }
} 