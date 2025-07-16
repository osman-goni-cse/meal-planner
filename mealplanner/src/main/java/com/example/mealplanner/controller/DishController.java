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
import org.springframework.web.bind.annotation.ResponseBody;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        model.addAttribute("mealPeriods", Arrays.asList("LUNCH", "SNACKS"));
        model.addAttribute("pageTitle", "Add New Dish");
        return "add-dish";
    }

    @PostMapping("/dishes/new")
    public String addDish(@ModelAttribute("dish") @jakarta.validation.Valid Dish dish,
                         org.springframework.validation.BindingResult bindingResult,
                         @RequestParam("imageFile") MultipartFile imageFile,
                         @RequestParam(value = "dietaryInfo", required = false) List<String> dietaryInfo,
                         Model model,
                         RedirectAttributes redirectAttributes) throws IOException {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", CATEGORIES);
            model.addAttribute("dietaryTags", DIETARY_TAGS);
            model.addAttribute("mealPeriods", Arrays.asList("LUNCH", "SNACKS"));
            model.addAttribute("pageTitle", "Add New Dish");
            return "add-dish";
        }
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
        redirectAttributes.addFlashAttribute("successMessage", "Dish added successfully!");
        return "redirect:/dishes";
    }

    @GetMapping("/dishes")
    public String listDishes(
        @RequestParam(value = "search", required = false) String search,
        @RequestParam(value = "mealPeriod", required = false) String mealPeriod,
        @RequestParam(value = "category", required = false) String category,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "8") int size,
        Model model, @AuthenticationPrincipal OAuth2User oauth2User, HttpServletRequest request) {
        logger.info("Dish management accessed by user: {}", oauth2User != null ? oauth2User.getAttribute("email") : "anonymous");
        logger.info("User authorities: {}", oauth2User != null ? oauth2User.getAuthorities() : "no authorities");
        logger.info("Request URI: {}", request.getRequestURI());
        Pageable pageable = PageRequest.of(page, size);
        Page<Dish> dishesPage = dishRepository.findByFilters(
            (search != null && !search.isBlank()) ? search : null,
            (mealPeriod != null && !mealPeriod.isBlank()) ? mealPeriod : null,
            (category != null && !category.isBlank()) ? category : null,
            pageable);
        model.addAttribute("dishesPage", dishesPage);
        model.addAttribute("dishes", dishesPage.getContent());
        model.addAttribute("dish", new Dish());
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("dietaryTags", DIETARY_TAGS); 
        model.addAttribute("pageTitle", "Dish Management");
        model.addAttribute("dishCount", dishesPage.getTotalElements());
        model.addAttribute("currentPath", request.getRequestURI());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("search", search);
        model.addAttribute("mealPeriod", mealPeriod);
        model.addAttribute("category", category);
        return "dish-management";
    }

    @PostMapping("/dishes/delete/{id}")
    public String deleteDish(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        dishRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Dish deleted successfully!");
        return "redirect:/dishes";
    }

    @GetMapping("/dishes/edit/{id}")
    public String showEditDishForm(@PathVariable Long id, Model model, @AuthenticationPrincipal OAuth2User oauth2User) {
        logger.info("Edit dish form accessed by user: {} for dish id: {}", oauth2User != null ? oauth2User.getAttribute("email") : "anonymous", id);
        
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dish not found with id: " + id));
        
        model.addAttribute("dish", dish);
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("dietaryTags", DIETARY_TAGS);
        model.addAttribute("mealPeriods", Arrays.asList("LUNCH", "SNACKS"));
        model.addAttribute("pageTitle", "Edit Dish");
        model.addAttribute("isEdit", true);
        
        return "dish-management";
    }

    @PostMapping("/dishes/edit/{id}")
    public String updateDish(@PathVariable Long id,
                           @ModelAttribute("dish") @jakarta.validation.Valid Dish dish,
                           org.springframework.validation.BindingResult bindingResult,
                           @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                           @RequestParam(value = "dietaryInfo", required = false) List<String> dietaryInfo,
                           Model model,
                           RedirectAttributes redirectAttributes) throws IOException {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", CATEGORIES);
            model.addAttribute("dietaryTags", DIETARY_TAGS);
            model.addAttribute("mealPeriods", Arrays.asList("LUNCH", "SNACKS"));
            model.addAttribute("pageTitle", "Edit Dish");
            model.addAttribute("isEdit", true);
            return "dish-management";
        }
        
        Dish existingDish = dishRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dish not found with id: " + id));
        
        // Update basic fields
        existingDish.setName(dish.getName());
        existingDish.setMealPeriod(dish.getMealPeriod());
        existingDish.setCategory(dish.getCategory());
        existingDish.setDescription(dish.getDescription());
        
        // Handle image upload (only if new image is provided)
        if (imageFile != null && !imageFile.isEmpty()) {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            imageFile.transferTo(filePath);
            existingDish.setImageUrl("/" + UPLOAD_DIR + fileName);
        }
        
        // Handle dietary info
        existingDish.setDietaryInfo(dietaryInfo != null ? dietaryInfo : new ArrayList<>());
        
        dishRepository.save(existingDish);
        redirectAttributes.addFlashAttribute("successMessage", "Dish updated successfully!");
        return "redirect:/dishes";
    }

    @GetMapping("/dishes/json/{id}")
    @ResponseBody
    public Dish getDishJson(@PathVariable Long id) {
        return dishRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dish not found with id: " + id));
    }
} 