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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class DishController {
    private static final List<String> CATEGORIES = Arrays.asList("Main Course", "Side Dish", "Beverage", "Dessert");
    private static final List<String> DIETARY_TAGS = Arrays.asList(
            "Vegetarian", "Vegan", "Gluten-Free", "Dairy-Free", "Nut-Free", "Low-Carb", "High-Protein", "Spicy"
    );
    private static final String UPLOAD_DIR = "uploads/";

    @Autowired
    private DishRepository dishRepository;

    @GetMapping("/dishes/new")
    public String showAddDishForm(Model model) {
        model.addAttribute("dish", new Dish());
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("dietaryTags", DIETARY_TAGS);
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
        return "redirect:/dishes/new?success";
    }

    @GetMapping("/dishes")
    public String listDishes(Model model) {
        List<Dish> dishes = dishRepository.findAll();
        model.addAttribute("dishes", dishes);
        return "dish-management";
    }

    @PostMapping("/dishes/delete/{id}")
    public String deleteDish(@PathVariable Long id) {
        dishRepository.deleteById(id);
        return "redirect:/dishes";
    }
} 