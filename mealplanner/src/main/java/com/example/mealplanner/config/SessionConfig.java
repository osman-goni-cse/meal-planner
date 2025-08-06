package com.example.mealplanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 1800) // 30 minutes - synchronized with cookie timeout
public class SessionConfig {

    /**
     * Cookie Serializer for session cookies
     * Configured to work with Chrome and prevent session sharing issues
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        
        // Basic cookie settings
        serializer.setCookieName("SESSION");
        serializer.setCookiePath("/");
        serializer.setCookieMaxAge(1800); // 30 minutes - synchronized with session timeout
        
        // Security settings
        serializer.setUseHttpOnlyCookie(true);
        serializer.setUseSecureCookie(true); // Set to true in production with HTTPS
        
        // CRITICAL: Chrome compatibility settings
        serializer.setSameSite("Lax"); // Required for Chrome
        
        // Disable base64 encoding to prevent cookie corruption
        serializer.setUseBase64Encoding(false);
        
        return serializer;
    }

    /**
     * HttpSessionEventPublisher for proper session lifecycle management
     * This is REQUIRED for maximumSessions functionality to work properly
     * It helps Spring Security track session creation and destruction events
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
} 