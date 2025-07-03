package com.example.mealplanner.controller;

import com.example.mealplanner.service.CommentService;
import com.example.mealplanner.dto.DishDetailsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.mealplanner.dto.DishCommentsPageDTO;

@Controller
public class ReviewCommentsController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/review-comments")
    public String reviewComments(Model model, HttpServletRequest request) {
        model.addAttribute("mostCommentedDishes", commentService.getMostCommentedDishes(8));
        model.addAttribute("currentPath", request.getRequestURI());

        model.addAttribute("pageTitle", "Review Comments");
        model.addAttribute("commentCount", commentService.getCommentCount());
        return "review-comments";
    }

    @GetMapping("/review-comments/dish/{id}")
    @ResponseBody
    public DishDetailsDTO getDishDetails(@PathVariable Long id) {
        return commentService.getDishDetailsWithComments(id);
    }

    @GetMapping("/review-comments/dish/{id}/comments")
    @ResponseBody
    public DishCommentsPageDTO getDishCommentsPage(
        @PathVariable Long id,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size
    ) {
        return commentService.getDishCommentsPage(id, page, size);
    }
} 