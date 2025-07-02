package com.example.mealplanner.controller;

import com.example.mealplanner.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;

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
} 