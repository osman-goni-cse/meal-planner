package com.example.mealplanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.mealplanner.service.CustomOidcUserService;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.access.AccessDeniedException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import com.example.mealplanner.config.CustomAuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public CustomOidcUserService customOidcUserService() {
        return new CustomOidcUserService();
    }

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/api/reactions")
            )

            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/login", "/error", "/webjars/**", "/css/**", "/js/**", "/images/**", "/test-auth", "/debug-user").permitAll()
                .requestMatchers("/weekly-plan/**", "/dishes/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/", "/weekly-feedback/**", "/uploads/**").permitAll() // Allow anonymous access to home and weekly-feedback
                .requestMatchers("/api/reactions", "/weekly-feedback/feedback").authenticated() // Require auth for interactions
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                    .oidcUserService(customOidcUserService())
                )
                .successHandler(customAuthenticationSuccessHandler)
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .accessDeniedHandler(accessDeniedHandler())
            )
            .requestCache(requestCache -> requestCache
                .requestCache(new org.springframework.security.web.savedrequest.HttpSessionRequestCache())
            );
        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new AccessDeniedHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response,
                             AccessDeniedException accessDeniedException) throws IOException, ServletException {
                System.out.println("=== ACCESS DENIED DEBUG ===");
                System.out.println("Request URI: " + request.getRequestURI());
                System.out.println("Request Method: " + request.getMethod());
                System.out.println("User Principal: " + (request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "null"));
                System.out.println("Exception: " + accessDeniedException.getMessage());
                System.out.println("==========================");
                response.sendRedirect("/access-denied");
            }
        };
    }
} 