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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public CustomOidcUserService customOidcUserService() {
        return new CustomOidcUserService();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/api/votes")
            )

            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/login", "/error", "/webjars/**", "/css/**", "/js/**", "/images/**", "/api/votes", "/test-auth", "/debug-user").permitAll()
                .requestMatchers("/dashboard", "/dashboard/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_EMPLOYEE")
                .requestMatchers("/weekly-plan/**", "/dishes/**").hasAuthority("ROLE_ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .userInfoEndpoint(userInfo -> userInfo
                    .oidcUserService(customOidcUserService())
                )
                .defaultSuccessUrl("/dashboard", true)
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .accessDeniedHandler(accessDeniedHandler())
            );
        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new AccessDeniedHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response,
                             AccessDeniedException accessDeniedException) throws IOException, ServletException {
                response.sendRedirect("/access-denied");
            }
        };
    }
} 