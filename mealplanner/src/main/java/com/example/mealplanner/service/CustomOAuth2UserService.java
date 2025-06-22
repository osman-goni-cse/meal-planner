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

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        String email = oauth2User.getAttribute("email");
        
        logger.info("Google OAuth2 login attempt for email: {}", email);

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                logger.error("User not found in database for email: {}", email);
                return new OAuth2AuthenticationException("User not found in database: " + email);
            });
        
        logger.info("User found in database: {} with role: {}", email, user.getRole());
        return new CustomUserDetails(user, oauth2User.getAttributes());
    }
} 