package com.taskify.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.taskify.model.User;
import com.taskify.repository.UserRepository;

/**
 * Tests unitaires pour UserService.
 * Utilise Mockito pour simuler les dépendances.
 * Pattern AAA: Arrange - Act - Assert
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() {
        // Arrange: Préparer un utilisateur de test
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword123");
    }

    // ==================== Tests pour register() ====================

    @Test
    @DisplayName("register() - Doit créer un utilisateur avec succès")
    void register_ShouldCreateUser_WhenEmailDoesNotExist() {
        // Arrange
        String email = "newuser@example.com";
        String password = "password123";
        String encodedPassword = "encodedPassword123";
        
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // Act
        User result = userService.register(email, password);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(encodedPassword, result.getPassword());
        verify(userRepository).existsByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register() - Doit échouer si l'email existe déjà")
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        String email = "existing@example.com";
        String password = "password123";
        
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.register(email, password));
        
        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository).existsByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== Tests pour findByEmail() ====================

    @Test
    @DisplayName("findByEmail() - Doit retourner l'utilisateur s'il existe")
    void findByEmail_ShouldReturnUser_WhenUserExists() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findByEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser.getEmail(), result.get().getEmail());
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("findByEmail() - Doit retourner Optional vide si l'utilisateur n'existe pas")
    void findByEmail_ShouldReturnEmpty_WhenUserDoesNotExist() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findByEmail(email);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByEmail(email);
    }

    // ==================== Tests pour authenticate() ====================

    @Test
    @DisplayName("authenticate() - Doit authentifier l'utilisateur avec des identifiants valides")
    void authenticate_ShouldReturnUser_WhenCredentialsAreValid() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPassword())).thenReturn(true);

        // Act
        User result = userService.authenticate(email, password);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, testUser.getPassword());
    }

    @Test
    @DisplayName("authenticate() - Doit échouer si l'email n'existe pas")
    void authenticate_ShouldThrowException_WhenEmailDoesNotExist() {
        // Arrange
        String email = "nonexistent@example.com";
        String password = "password123";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userService.authenticate(email, password));
        
        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("authenticate() - Doit échouer si le mot de passe est incorrect")
    void authenticate_ShouldThrowException_WhenPasswordIsIncorrect() {
        // Arrange
        String email = "test@example.com";
        String wrongPassword = "wrongPassword";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(wrongPassword, testUser.getPassword())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userService.authenticate(email, wrongPassword));
        
        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(wrongPassword, testUser.getPassword());
    }
}
