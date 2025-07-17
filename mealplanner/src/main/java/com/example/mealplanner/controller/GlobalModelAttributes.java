package com.example.mealplanner.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice
public class GlobalModelAttributes {
    @ModelAttribute
    public void addGlobalAttributes(Model model, @AuthenticationPrincipal OAuth2User oauth2User) {
        boolean isAuthenticated = oauth2User != null;
        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("userName", isAuthenticated ? oauth2User.getAttribute("name") : "Innovators");
    }
} 