package com.example.mealplanner.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
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
        response.sendRedirect(redirectUrl);
    }

    private boolean isSafeRedirect(String url) {
        if (url == null) return false;
        if (url.startsWith("http://") || url.startsWith("https://")) return false;
        if (url.startsWith("/api/") || url.endsWith(".json")) return false;
        return true;
    }
} 