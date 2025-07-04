package com.example.mealplanner.controller;

import com.example.mealplanner.model.Dish;
import com.example.mealplanner.repository.DishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class DishController {
    private static final Logger logger = LoggerFactory.getLogger(DishController.class);
    private static final List<String> CATEGORIES = Arrays.asList("Main Course", "Side Dish", "Beverage", "Dessert");
    private static final List<String> DIETARY_TAGS = Arrays.asList(
            "Vegetarian", "Vegan", "Gluten-Free", "Dairy-Free", "Nut-Free", "Low-Carb", "High-Protein", "Spicy"
    );
    private static final String UPLOAD_DIR = "uploads/";

    @Autowired
    private DishRepository dishRepository;

    @GetMapping("/dishes/new")
    public String showAddDishForm(Model model, @AuthenticationPrincipal OAuth2User oauth2User) {
        logger.info("Add dish form accessed by user: {}", oauth2User != null ? oauth2User.getAttribute("email") : "anonymous");
        model.addAttribute("dish", new Dish());
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("dietaryTags", DIETARY_TAGS);
        model.addAttribute("pageTitle", "Add New Dish");
        return "add-dish";
    }

    @PostMapping("/dishes/new")
    public String addDish(@ModelAttribute Dish dish,
                         @RequestParam("imageFile") MultipartFile imageFile,
                         @RequestParam(value = "dietaryInfo", required = false) List<String> dietaryInfo,
                         Model model) throws IOException {
        // Handle image upload
        if (!imageFile.isEmpty()) {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            imageFile.transferTo(filePath);
            dish.setImageUrl("/" + UPLOAD_DIR + fileName);
        }
        // Handle dietary info
        dish.setDietaryInfo(dietaryInfo != null ? dietaryInfo : new ArrayList<>());
        dishRepository.save(dish);
        return "redirect:/dishes";
    }

    @GetMapping("/dishes")
    public String listDishes(Model model, @AuthenticationPrincipal OAuth2User oauth2User, HttpServletRequest request) {
        logger.info("Dish management accessed by user: {}", oauth2User != null ? oauth2User.getAttribute("email") : "anonymous");
        List<Dish> dishes = dishRepository.findAll();
        model.addAttribute("dishes", dishes);
        model.addAttribute("dish", new Dish());
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("dietaryTags", DIETARY_TAGS); 
        model.addAttribute("pageTitle", "Dish Management");
        model.addAttribute("dishCount", dishes.size());
        model.addAttribute("currentPath", request.getRequestURI());
        return "dish-management";
    }

    @PostMapping("/dishes/delete/{id}")
    public String deleteDish(@PathVariable Long id) {
        dishRepository.deleteById(id);
        return "redirect:/dishes";
    }
} 