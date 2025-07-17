package com.example.mealplanner.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, 
                       @RequestParam(value = "logout", required = false) String logout,
                       @RequestParam(value = "redirect", required = false) String redirect,
                       Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Authentication failed. Please check if your email is registered in the system.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been successfully signed out.");
        }
        if (redirect != null) {
            model.addAttribute("redirectUrl", redirect);
        }
        return "login";
    }
} 