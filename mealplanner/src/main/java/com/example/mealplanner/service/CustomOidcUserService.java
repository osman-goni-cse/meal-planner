package com.example.mealplanner.service;

import com.example.mealplanner.model.User;
import com.example.mealplanner.repository.UserRepository;
import com.example.mealplanner.model.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;

public class CustomOidcUserService extends OidcUserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOidcUserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserOnboardingService userOnboardingService;

    public CustomOidcUserService() {
        logger.info("=== CustomOidcUserService constructor called ===");
    }

    @PostConstruct
    public void init() {
        logger.info("=== CustomOidcUserService @PostConstruct called ===");
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        logger.info("=== CustomOidcUserService.loadUser() called ===");
        logger.info("UserRequest: {}", userRequest.getClientRegistration().getRegistrationId());
        
        OidcUser oidcUser = super.loadUser(userRequest);
        String email = oidcUser.getEmail();
        String profileImageUrl = (String) oidcUser.getAttribute("picture");
        
        logger.info("Google OIDC login attempt for email: {}", email);
        logger.info("Original OidcUser authorities: {}", oidcUser.getAuthorities());

        try {
            // Use the onboarding service to handle user creation/retrieval
            User user = userOnboardingService.onboardUser(email, profileImageUrl);
            
            logger.info("User processed through onboarding: {} with role: {}", email, user.getRole());
            
            CustomUserDetails customUserDetails = new CustomUserDetails(user, oidcUser.getAttributes());
            logger.info("Created CustomUserDetails with authorities: {}", customUserDetails.getAuthorities());
            logger.info("=== CustomOidcUserService.loadUser() completed ===");
            
            return customUserDetails;
        } catch (Exception e) {
            logger.error("Error in CustomOidcUserService.loadUser(): ", e);
            throw e;
        }
    }
} 