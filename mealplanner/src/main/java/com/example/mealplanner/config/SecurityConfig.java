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
import com.example.mealplanner.service.CustomAuthenticationSuccessHandler;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

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
                .csrfTokenRepository(createCsrfTokenRepository())
            )

            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/login", "/error", "/webjars/**", "/css/**", "/js/**", "/images/**", "/test-auth", "/debug-user").permitAll()
                .requestMatchers("/weekly-plan/**", "/dishes/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/", "/weekly-feedback/**", "/food-committee", "/uploads/**").permitAll() // Allow anonymous access to home and weekly-feedback
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
                .logoutSuccessUrl("/")
                .deleteCookies("SESSION", "XSRF-TOKEN")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation().changeSessionId() // ENABLE session fixation protection
                .maximumSessions(1)
                .maxSessionsPreventsLogin(true) // Prevent multiple sessions per user
                .expiredUrl("/login")
            )
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .accessDeniedHandler(accessDeniedHandler())
            )
            .requestCache(requestCache -> requestCache
                .requestCache(httpSessionRequestCache())
            );
        return http.build();
    }

    @Bean
    public RequestCache httpSessionRequestCache() {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setCreateSessionAllowed(false); // Prevent session creation during request caching
        return requestCache;
    }

    @Bean
    public CookieCsrfTokenRepository createCsrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieName("XSRF-TOKEN");
        repository.setCookiePath("/");
        repository.setCookieHttpOnly(false);
        // Note: setCookieSecure and setCookieSameSite not available in this Spring Boot version
        // These will be handled by the session cookie configuration instead
        return repository;
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