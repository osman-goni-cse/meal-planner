package com.example.mealplanner.service;

import com.example.mealplanner.dto.DishCommentStatsDTO;
import com.example.mealplanner.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    public List<DishCommentStatsDTO> getMostCommentedDishes(int limit) {
        List<Object[]> results = feedbackRepository.findDishCommentStats(PageRequest.of(0, limit));
        List<DishCommentStatsDTO> dtos = new ArrayList<>();
        for (Object[] row : results) {
            DishCommentStatsDTO dto = new DishCommentStatsDTO();
            dto.setId((Long) row[0]);
            dto.setName((String) row[1]);
            dto.setImageUrl((String) row[2]);
            dto.setCommentsCount(((Number) row[3]).intValue());
            dto.setLikes(0); // If you have likes, set here
            dtos.add(dto);
        }
        return dtos;
    }

    public int getCommentCount() {
        return feedbackRepository.findAll().size();
    }
} 