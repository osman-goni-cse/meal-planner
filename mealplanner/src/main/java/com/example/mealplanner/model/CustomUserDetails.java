package com.example.mealplanner.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CustomUserDetails implements OAuth2User, UserDetails, OidcUser {

    private final User user;
    private final Map<String, Object> attributes;
    private final OidcIdToken idToken;
    private final OidcUserInfo userInfo;

    public CustomUserDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
        this.idToken = null; // We don't need the ID token for our use case
        this.userInfo = null; // We don't need the user info for our use case
    }

    // OAuth2User methods
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return user.getEmail();
    }

    // OidcUser methods
    @Override
    public OidcIdToken getIdToken() {
        return idToken;
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return userInfo;
    }

    @Override
    public Map<String, Object> getClaims() {
        return attributes;
    }

    // UserDetails methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security expects roles to be prefixed with "ROLE_"
        String roleWithPrefix = "ROLE_" + user.getRole().toUpperCase();
        return Collections.singletonList(new SimpleGrantedAuthority(roleWithPrefix));
    }

    @Override
    public String getPassword() {
        return null; // Not needed for OAuth2
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // Custom method to get the underlying User object
    public User getUser() {
        return user;
    }

    // Custom method to get the user's role for Thymeleaf access
    public String getRole() {
        return user.getRole();
    }

    public Long getId() {
        return user.getId();
    }
} 