package com.example.mealplanner.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.mealplanner.model.User;
import com.example.mealplanner.repository.UserRepository;

@Controller
public class HomeController {
    
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("pageTitle", "Access Denied");
        return "access-denied";
    }
    
    @GetMapping("/test-auth")
    @ResponseBody
    public String testAuth(@AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User == null) {
            return "Not authenticated";
        }
        
        String email = oauth2User.getAttribute("email");
        String authorities = oauth2User.getAuthorities().toString();
        
        logger.info("Test auth - Email: {}, Authorities: {}", email, authorities);
        
        return String.format("Authenticated as: %s<br>Authorities: %s", email, authorities);
    }
    
    @GetMapping("/debug-user")
    @ResponseBody
    public String debugUser(@AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User == null) {
            return "Not authenticated";
        }
        
        String email = oauth2User.getAttribute("email");
        
        // Check if user exists in database
        var userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return String.format("User found in database:<br>Email: %s<br>Role: %s<br>ID: %s", 
                               user.getEmail(), user.getRole(), user.getId());
        } else {
            // Create user with Admin role if not exists
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setRole("Admin");
            userRepository.save(newUser);
            
            return String.format("User not found in database. Created new user:<br>Email: %s<br>Role: %s<br>ID: %s", 
                               newUser.getEmail(), newUser.getRole(), newUser.getId());
        }
    }
} 