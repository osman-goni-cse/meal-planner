package com.example.mealplanner.service;

import com.example.mealplanner.dto.DishCommentStatsDTO;
import com.example.mealplanner.repository.FeedbackRepository;
import com.example.mealplanner.dto.DishDetailsDTO;
import com.example.mealplanner.dto.DishCommentDTO;
import com.example.mealplanner.repository.DishRepository;
import com.example.mealplanner.model.Dish;
import com.example.mealplanner.model.Feedback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.example.mealplanner.dto.DishCommentsPageDTO;

@Service
public class CommentService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private DishRepository dishRepository;

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

    public DishDetailsDTO getDishDetailsWithComments(Long dishId) {
        Dish dish = dishRepository.findById(dishId).orElseThrow();
        List<Feedback> feedbacks = feedbackRepository.findAll().stream()
            .filter(fb -> dishId.equals(fb.getDishId()))
            .collect(Collectors.toList());

        List<DishCommentDTO> comments = feedbacks.stream().map(fb -> {
            DishCommentDTO dto = new DishCommentDTO();
            dto.setUserName(fb.getUserName() != null ? fb.getUserName() : "Anonymous");
            dto.setAvatarUrl(fb.getUserAvatarUrl() != null ? fb.getUserAvatarUrl() : "/static/default-avatar.png");
            dto.setText(fb.getComment());
            return dto;
        }).collect(Collectors.toList());

        DishDetailsDTO dto = new DishDetailsDTO();
        dto.setName(dish.getName());
        dto.setDescription(dish.getDescription());
        dto.setImageUrl(dish.getImageUrl());
        dto.setComments(comments);
        return dto;
    }

    public DishCommentsPageDTO getDishCommentsPage(Long dishId, int page, int size) {
        List<Feedback> all = feedbackRepository.findAll().stream()
            .filter(fb -> dishId.equals(fb.getDishId()))
            .collect(Collectors.toList());
        int from = page * size;
        int to = Math.min(from + size, all.size());
        List<DishCommentDTO> comments = all.subList(from, to).stream().map(fb -> {
            DishCommentDTO dto = new DishCommentDTO();
            dto.setUserName(fb.getUserName() != null ? fb.getUserName() : "Anonymous");
            dto.setAvatarUrl(fb.getUserAvatarUrl() != null ? fb.getUserAvatarUrl() : "/static/default-avatar.png");
            dto.setText(fb.getComment());
            return dto;
        }).collect(Collectors.toList());
        DishCommentsPageDTO dto = new DishCommentsPageDTO();
        dto.setComments(comments);
        dto.setHasMore(to < all.size());
        return dto;
    }
} 