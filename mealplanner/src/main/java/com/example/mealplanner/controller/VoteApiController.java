package com.example.mealplanner.controller;

import com.example.mealplanner.model.CustomUserDetails;
import com.example.mealplanner.model.VoteRequest;
import com.example.mealplanner.model.VoteResponse;
import com.example.mealplanner.model.VoteType;
import com.example.mealplanner.model.User;
import com.example.mealplanner.service.MealService;
import com.example.mealplanner.service.VoteService;
import com.example.mealplanner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/votes")
public class VoteApiController {

    @Autowired
    private VoteService voteService;

    @Autowired
    private MealService mealService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> handleVote(@RequestBody VoteRequest voteRequest, @AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        String email = oauth2User.getAttribute("email");
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body("User not found in database");
        }

        LocalDate today = LocalDate.now();
        String currentMealPeriod = mealService.getCurrentMealPeriod(LocalTime.now());

        if (currentMealPeriod == null) {
            return ResponseEntity.badRequest().body("Voting is not open at this time.");
        }

        voteService.vote(user.getId(), voteRequest.getDishId(), voteRequest.getVoteType(), today, currentMealPeriod);

        long upvotes = voteService.getUpvotes(voteRequest.getDishId(), today, currentMealPeriod);
        long downvotes = voteService.getDownvotes(voteRequest.getDishId(), today, currentMealPeriod);
        VoteType currentUserVote = voteService.getCurrentUserVote(user.getId(), voteRequest.getDishId(), today, currentMealPeriod);

        VoteResponse response = new VoteResponse(voteRequest.getDishId(), upvotes, downvotes, currentUserVote);
        return ResponseEntity.ok(response);
    }
} 