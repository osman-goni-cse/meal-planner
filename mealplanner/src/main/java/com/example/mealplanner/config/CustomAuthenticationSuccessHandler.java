package com.example.mealplanner.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        // Get session BEFORE any processing
        HttpSession session = request.getSession(false);
        String originalSessionId = session != null ? session.getId() : "NO_SESSION";
        
        logger.info("=== AUTHENTICATION SUCCESS ===");
        logger.info("User: {}", authentication.getName());
        logger.info("Original Session ID: {}", originalSessionId);
        logger.info("User Agent: {}", request.getHeader("User-Agent"));
        
        String redirectUrl = "/";
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));
        if (isAdmin) {
            redirectUrl = "/weekly-plan";
        } else {
            String cookieRedirect = null;
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("redirectAfterLogin".equals(cookie.getName())) {
                        cookieRedirect = java.net.URLDecoder.decode(cookie.getValue(), java.nio.charset.StandardCharsets.UTF_8);
                        // Clear the cookie
                        cookie.setMaxAge(0);
                        cookie.setPath("/");
                        response.addCookie(cookie);
                        break;
                    }
                }
            }
            if (cookieRedirect != null && !cookieRedirect.isEmpty() && isSafeRedirect(cookieRedirect)) {
                redirectUrl = cookieRedirect;
            }
        }
        
        // Check session AFTER processing
        HttpSession finalSession = request.getSession(false);
        String finalSessionId = finalSession != null ? finalSession.getId() : "NO_SESSION";
        
        if (!originalSessionId.equals(finalSessionId)) {
            logger.warn("SESSION ID CHANGED! Original: {} -> Final: {}", originalSessionId, finalSessionId);
            logger.warn("This indicates session fixation protection is regenerating session IDs!");
        } else {
            logger.info("Session ID remained stable: {}", finalSessionId);
        }
        
        logger.info("Redirecting to: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    private boolean isSafeRedirect(String url) {
        if (url == null) return false;
        if (url.startsWith("http://") || url.startsWith("https://")) return false;
        if (url.startsWith("/api/") || url.endsWith(".json")) return false;
        return true;
    }
} 