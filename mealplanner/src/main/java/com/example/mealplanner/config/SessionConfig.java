package com.example.mealplanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class SessionConfig {

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        
        // Basic cookie settings
        serializer.setCookieName("SESSION");
        serializer.setCookiePath("/");
        serializer.setCookieMaxAge(3600); // 1 hour for better stability
        
        // Security settings
        serializer.setUseHttpOnlyCookie(true);
        serializer.setUseSecureCookie(false); // Set to true in production with HTTPS
        
        // CRITICAL: Chrome compatibility settings
        serializer.setSameSite("Lax"); // Required for Chrome
        serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$"); // For your domain
        
        // Disable base64 encoding to prevent cookie corruption
        serializer.setUseBase64Encoding(false);
        
        return serializer;
    }
} 