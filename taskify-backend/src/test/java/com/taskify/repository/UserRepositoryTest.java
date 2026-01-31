package com.taskify.repository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.taskify.model.User;

/**
 * Tests du repository pour UserRepository.
 * Utilise @DataJpaTest pour tester avec une base H2 en mémoire.
 * Pattern AAA: Arrange - Act - Assert
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests Repository - UserRepository")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() {
        // Arrange: Préparer les données de test
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword123");
    }

    // ==================== Tests pour save() ====================

    @Test
    @DisplayName("save() - Doit sauvegarder un utilisateur avec succès")
    void save_ShouldPersistUser() {
        // Act
        User savedUser = userRepository.save(testUser);

        // Assert
        assertNotNull(savedUser.getId());
        assertEquals(testUser.getEmail(), savedUser.getEmail());
        assertEquals(testUser.getPassword(), savedUser.getPassword());
    }

    @Test
    @DisplayName("save() - Doit générer un ID automatiquement")
    void save_ShouldGenerateId() {
        // Act
        User savedUser = userRepository.save(testUser);

        // Assert
        assertNotNull(savedUser.getId());
        assertTrue(savedUser.getId() > 0);
    }

    // ==================== Tests pour findByEmail() ====================

    @Test
    @DisplayName("findByEmail() - Doit retourner l'utilisateur s'il existe")
    void findByEmail_ShouldReturnUser_WhenEmailExists() {
        // Arrange
        entityManager.persistAndFlush(testUser);

        // Act
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getEmail(), foundUser.get().getEmail());
    }

    @Test
    @DisplayName("findByEmail() - Doit retourner Optional vide si l'email n'existe pas")
    void findByEmail_ShouldReturnEmpty_WhenEmailDoesNotExist() {
        // Act
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertFalse(foundUser.isPresent());
    }

    // ==================== Tests pour existsByEmail() ====================

    @Test
    @DisplayName("existsByEmail() - Doit retourner true si l'email existe")
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
        // Arrange
        entityManager.persistAndFlush(testUser);

        // Act
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Assert
        assertTrue(exists);
    }

    @Test
    @DisplayName("existsByEmail() - Doit retourner false si l'email n'existe pas")
    void existsByEmail_ShouldReturnFalse_WhenEmailDoesNotExist() {
        // Act
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Assert
        assertFalse(exists);
    }

    // ==================== Tests pour findById() ====================

    @Test
    @DisplayName("findById() - Doit retourner l'utilisateur s'il existe")
    void findById_ShouldReturnUser_WhenIdExists() {
        // Arrange
        User persistedUser = entityManager.persistAndFlush(testUser);

        // Act
        Optional<User> foundUser = userRepository.findById(persistedUser.getId());

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(persistedUser.getId(), foundUser.get().getId());
    }

    @Test
    @DisplayName("findById() - Doit retourner Optional vide si l'ID n'existe pas")
    void findById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        // Act
        Optional<User> foundUser = userRepository.findById(99999L);

        // Assert
        assertFalse(foundUser.isPresent());
    }

    // ==================== Tests pour findAll() ====================

    @Test
    @DisplayName("findAll() - Doit retourner tous les utilisateurs")
    void findAll_ShouldReturnAllUsers() {
        // Arrange
        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setPassword("password2");

        entityManager.persistAndFlush(testUser);
        entityManager.persistAndFlush(user2);

        // Act
        List<User> users = userRepository.findAll();

        // Assert
        assertEquals(2, users.size());
    }

    // ==================== Tests pour delete() ====================

    @Test
    @DisplayName("delete() - Doit supprimer l'utilisateur")
    void delete_ShouldRemoveUser() {
        // Arrange
        User persistedUser = entityManager.persistAndFlush(testUser);
        Long userId = persistedUser.getId();

        // Act
        userRepository.delete(persistedUser);
        entityManager.flush();

        // Assert
        Optional<User> deletedUser = userRepository.findById(userId);
        assertFalse(deletedUser.isPresent());
    }

    // ==================== Tests pour update ====================

    @Test
    @DisplayName("save() - Doit mettre à jour un utilisateur existant")
    void save_ShouldUpdateUser_WhenUserExists() {
        // Arrange
        User persistedUser = entityManager.persistAndFlush(testUser);

        // Act
        persistedUser.setEmail("updated@example.com");
        User updatedUser = userRepository.save(persistedUser);
        entityManager.flush();

        // Assert
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals(persistedUser.getId(), updatedUser.getId());
    }
}
