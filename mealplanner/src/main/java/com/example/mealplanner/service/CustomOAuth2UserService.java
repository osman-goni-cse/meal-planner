package com.example.mealplanner.service;

import com.example.mealplanner.model.User;
import com.example.mealplanner.repository.UserRepository;
import com.example.mealplanner.model.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserOnboardingService userOnboardingService;

    public CustomOAuth2UserService() {
        logger.info("=== CustomOAuth2UserService constructor called ===");
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        logger.info("=== CustomOAuth2UserService.loadUser() called ===");
        logger.info("UserRequest: {}", userRequest.getClientRegistration().getRegistrationId());
        
        OAuth2User oauth2User = super.loadUser(userRequest);
        String email = oauth2User.getAttribute("email");
        
        logger.info("Google OAuth2 login attempt for email: {}", email);
        logger.info("Original OAuth2User authorities: {}", oauth2User.getAuthorities());

        try {
            // Use the onboarding service to handle user creation/retrieval
            User user = userOnboardingService.onboardUser(email);
            
            logger.info("User processed through onboarding: {} with role: {}", email, user.getRole());
            
            CustomUserDetails customUserDetails = new CustomUserDetails(user, oauth2User.getAttributes());
            logger.info("Created CustomUserDetails with authorities: {}", customUserDetails.getAuthorities());
            logger.info("=== CustomOAuth2UserService.loadUser() completed ===");
            
            return customUserDetails;
        } catch (Exception e) {
            logger.error("Error in CustomOAuth2UserService.loadUser(): ", e);
            throw e;
        }
    }
} 