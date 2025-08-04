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
import com.example.mealplanner.dto.DishCommentStatsDTO;
import java.util.List;

@Controller
public class ReviewCommentsController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/review-comments")
    public String reviewComments(
            @RequestParam(value = "search", required = false) String searchTerm,
            @RequestParam(value = "sort", required = false) String sortBy,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "8") int size,
            Model model, 
            HttpServletRequest request) {
        // Default to most commented if no sort specified
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "most-comments";
        }
        // Fetch paginated, filtered dishes
        var pagedDishes = commentService.getDishesWithSearchAndSortPaged(searchTerm, sortBy, page, size);
        model.addAttribute("mostCommentedDishes", pagedDishes.getContent());
        model.addAttribute("dishesPage", pagedDishes);
        model.addAttribute("currentPath", request.getRequestURI());
        model.addAttribute("pageTitle", "Review Feedbacks");
        // Use new count method that includes dishes with comments OR reactions
        long totalDishes = searchTerm != null && !searchTerm.trim().isEmpty() 
            ? commentService.getDishCountWithCommentsOrReactionsBySearch(searchTerm.trim())
            : commentService.getDishCountWithCommentsOrReactions();
        model.addAttribute("totalDishes", totalDishes);
        model.addAttribute("commentCount", commentService.getCommentCount());
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        return "review-comments";
    }

    @GetMapping("/review-comments/search")
    @ResponseBody
    public List<DishCommentStatsDTO> searchDishes(
            @RequestParam(value = "search", required = false) String searchTerm,
            @RequestParam(value = "sort", required = false) String sortBy,
            @RequestParam(value = "limit", defaultValue = "8") int limit) {
        
        // Default to most commented if no sort specified
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "most-comments";
        }
        
        return commentService.getDishesWithSearchAndSort(searchTerm, sortBy, limit);
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