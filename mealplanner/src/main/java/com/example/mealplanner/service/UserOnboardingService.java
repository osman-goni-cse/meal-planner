package com.example.mealplanner.service;

import com.example.mealplanner.model.User;
import com.example.mealplanner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Service
public class UserOnboardingService {

    private static final Logger logger = LoggerFactory.getLogger(UserOnboardingService.class);
    private static final String DEFAULT_ROLE = "EMPLOYEE";

    @Autowired
    private UserRepository userRepository;

    /**
     * Onboards a user by checking if they exist in the database.
     * If the user doesn't exist, creates a new user with EMPLOYEE role.
     * If the user exists, returns the existing user.
     * 
     * @param email The user's email address
     * @return The user (either existing or newly created)
     */
    public User onboardUser(String email) {
        return onboardUser(email, null);
    }

    /**
     * Onboards a user by checking if they exist in the database.
     * If the user doesn't exist, creates a new user with EMPLOYEE role and profile image URL.
     * If the user exists, updates the profile image URL if changed.
     *
     * @param email The user's email address
     * @param profileImageUrl The user's Google profile image URL
     * @return The user (either existing or newly created)
     */
    public User onboardUser(String email, String profileImageUrl) {
        logger.info("Starting user onboarding process for email: {}", email);
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (profileImageUrl != null && !profileImageUrl.equals(user.getProfileImageUrl())) {
                user.setProfileImageUrl(profileImageUrl);
                userRepository.save(user);
                logger.info("Updated profile image URL for user: {}", email);
            }
            logger.info("Existing user found: {} with role: {}", email, user.getRole());
            return user;
        } else {
            logger.info("New user detected: {}. Creating user with {} role.", email, DEFAULT_ROLE);
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setRole(DEFAULT_ROLE);
            newUser.setProfileImageUrl(profileImageUrl);
            User savedUser = userRepository.save(newUser);
            logger.info("Successfully created new user: {} with role: {} and ID: {}", 
                       email, savedUser.getRole(), savedUser.getId());
            return savedUser;
        }
    }

    /**
     * Checks if a user exists in the database.
     * 
     * @param email The user's email address
     * @return true if user exists, false otherwise
     */
    public boolean userExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
} 