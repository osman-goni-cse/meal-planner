package com.example.mealplanner.service;

import com.example.mealplanner.model.User;
import com.example.mealplanner.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserOnboardingServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserOnboardingService userOnboardingService;

    private User existingUser;
    private User newUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("existing@example.com");
        existingUser.setRole("ADMIN");

        newUser = new User();
        newUser.setId(2L);
        newUser.setEmail("new@example.com");
        newUser.setRole("EMPLOYEE");
    }

    @Test
    void onboardUser_ExistingUser_ReturnsExistingUser() {
        // Arrange
        String email = "existing@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        // Act
        User result = userOnboardingService.onboardUser(email);

        // Assert
        assertNotNull(result);
        assertEquals(existingUser.getId(), result.getId());
        assertEquals(existingUser.getEmail(), result.getEmail());
        assertEquals(existingUser.getRole(), result.getRole());
        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void onboardUser_NewUser_CreatesAndReturnsNewUser() {
        // Arrange
        String email = "new@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // Act
        User result = userOnboardingService.onboardUser(email);

        // Assert
        assertNotNull(result);
        assertEquals(newUser.getId(), result.getId());
        assertEquals(newUser.getEmail(), result.getEmail());
        assertEquals("EMPLOYEE", result.getRole());
        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void userExists_ExistingUser_ReturnsTrue() {
        // Arrange
        String email = "existing@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        // Act
        boolean result = userOnboardingService.userExists(email);

        // Assert
        assertTrue(result);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void userExists_NewUser_ReturnsFalse() {
        // Arrange
        String email = "new@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        boolean result = userOnboardingService.userExists(email);

        // Assert
        assertFalse(result);
        verify(userRepository, times(1)).findByEmail(email);
    }
} 