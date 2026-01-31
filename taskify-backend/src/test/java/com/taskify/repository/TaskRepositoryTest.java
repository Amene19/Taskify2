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

import com.taskify.model.Task;
import com.taskify.model.TaskStatus;
import com.taskify.model.User;

/**
 * Tests du repository pour TaskRepository.
 * Utilise @DataJpaTest pour tester avec une base H2 en mémoire.
 * Pattern AAA: Arrange - Act - Assert
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests Repository - TaskRepository")
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    private User testUser;
    private User otherUser;
    private Task testTask;

    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() {
        // Arrange: Préparer les données de test
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        entityManager.persistAndFlush(testUser);

        otherUser = new User();
        otherUser.setEmail("other@example.com");
        otherUser.setPassword("password456");
        entityManager.persistAndFlush(otherUser);

        testTask = new Task();
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setStatus(TaskStatus.TODO);
        testTask.setUser(testUser);
    }

    // ==================== Tests pour save() ====================

    @Test
    @DisplayName("save() - Doit sauvegarder une tâche avec succès")
    void save_ShouldPersistTask() {
        // Act
        Task savedTask = taskRepository.save(testTask);

        // Assert
        assertNotNull(savedTask.getId());
        assertEquals(testTask.getTitle(), savedTask.getTitle());
        assertEquals(testTask.getDescription(), savedTask.getDescription());
        assertEquals(testTask.getStatus(), savedTask.getStatus());
        assertEquals(testUser.getId(), savedTask.getUser().getId());
    }

    @Test
    @DisplayName("save() - Doit générer un ID automatiquement")
    void save_ShouldGenerateId() {
        // Act
        Task savedTask = taskRepository.save(testTask);

        // Assert
        assertNotNull(savedTask.getId());
        assertTrue(savedTask.getId() > 0);
    }

    // ==================== Tests pour findByUser() ====================

    @Test
    @DisplayName("findByUser() - Doit retourner toutes les tâches de l'utilisateur")
    void findByUser_ShouldReturnAllTasksForUser() {
        // Arrange
        Task task2 = new Task();
        task2.setTitle("Task 2");
        task2.setDescription("Description 2");
        task2.setStatus(TaskStatus.DONE);
        task2.setUser(testUser);

        entityManager.persistAndFlush(testTask);
        entityManager.persistAndFlush(task2);

        // Act
        List<Task> tasks = taskRepository.findByUser(testUser);

        // Assert
        assertEquals(2, tasks.size());
        assertTrue(tasks.stream().allMatch(t -> t.getUser().getId().equals(testUser.getId())));
    }

    @Test
    @DisplayName("findByUser() - Doit retourner une liste vide si l'utilisateur n'a pas de tâches")
    void findByUser_ShouldReturnEmptyList_WhenUserHasNoTasks() {
        // Act
        List<Task> tasks = taskRepository.findByUser(otherUser);

        // Assert
        assertTrue(tasks.isEmpty());
    }

    @Test
    @DisplayName("findByUser() - Ne doit pas retourner les tâches d'autres utilisateurs")
    void findByUser_ShouldNotReturnOtherUsersTasks() {
        // Arrange
        entityManager.persistAndFlush(testTask);

        Task otherTask = new Task();
        otherTask.setTitle("Other User Task");
        otherTask.setDescription("Other Description");
        otherTask.setStatus(TaskStatus.TODO);
        otherTask.setUser(otherUser);
        entityManager.persistAndFlush(otherTask);

        // Act
        List<Task> testUserTasks = taskRepository.findByUser(testUser);
        List<Task> otherUserTasks = taskRepository.findByUser(otherUser);

        // Assert
        assertEquals(1, testUserTasks.size());
        assertEquals("Test Task", testUserTasks.get(0).getTitle());
        assertEquals(1, otherUserTasks.size());
        assertEquals("Other User Task", otherUserTasks.get(0).getTitle());
    }

    // ==================== Tests pour findByIdAndUser() ====================

    @Test
    @DisplayName("findByIdAndUser() - Doit retourner la tâche si elle appartient à l'utilisateur")
    void findByIdAndUser_ShouldReturnTask_WhenTaskBelongsToUser() {
        // Arrange
        Task persistedTask = entityManager.persistAndFlush(testTask);

        // Act
        Optional<Task> foundTask = taskRepository.findByIdAndUser(persistedTask.getId(), testUser);

        // Assert
        assertTrue(foundTask.isPresent());
        assertEquals(persistedTask.getId(), foundTask.get().getId());
    }

    @Test
    @DisplayName("findByIdAndUser() - Doit retourner Optional vide si la tâche n'appartient pas à l'utilisateur")
    void findByIdAndUser_ShouldReturnEmpty_WhenTaskDoesNotBelongToUser() {
        // Arrange
        Task persistedTask = entityManager.persistAndFlush(testTask);

        // Act
        Optional<Task> foundTask = taskRepository.findByIdAndUser(persistedTask.getId(), otherUser);

        // Assert
        assertFalse(foundTask.isPresent());
    }

    @Test
    @DisplayName("findByIdAndUser() - Doit retourner Optional vide si la tâche n'existe pas")
    void findByIdAndUser_ShouldReturnEmpty_WhenTaskDoesNotExist() {
        // Act
        Optional<Task> foundTask = taskRepository.findByIdAndUser(99999L, testUser);

        // Assert
        assertFalse(foundTask.isPresent());
    }

    // ==================== Tests pour findByUserId() ====================

    @Test
    @DisplayName("findByUserId() - Doit retourner toutes les tâches par ID utilisateur")
    void findByUserId_ShouldReturnAllTasksForUserId() {
        // Arrange
        entityManager.persistAndFlush(testTask);

        Task task2 = new Task();
        task2.setTitle("Task 2");
        task2.setDescription("Description 2");
        task2.setStatus(TaskStatus.DONE);
        task2.setUser(testUser);
        entityManager.persistAndFlush(task2);

        // Act
        List<Task> tasks = taskRepository.findByUserId(testUser.getId());

        // Assert
        assertEquals(2, tasks.size());
    }

    // ==================== Tests pour delete() ====================

    @Test
    @DisplayName("delete() - Doit supprimer la tâche")
    void delete_ShouldRemoveTask() {
        // Arrange
        Task persistedTask = entityManager.persistAndFlush(testTask);
        Long taskId = persistedTask.getId();

        // Act
        taskRepository.delete(persistedTask);
        entityManager.flush();

        // Assert
        Optional<Task> deletedTask = taskRepository.findById(taskId);
        assertFalse(deletedTask.isPresent());
    }

    // ==================== Tests pour update ====================

    @Test
    @DisplayName("save() - Doit mettre à jour une tâche existante")
    void save_ShouldUpdateTask_WhenTaskExists() {
        // Arrange
        Task persistedTask = entityManager.persistAndFlush(testTask);

        // Act
        persistedTask.setTitle("Updated Title");
        persistedTask.setStatus(TaskStatus.DONE);
        Task updatedTask = taskRepository.save(persistedTask);
        entityManager.flush();

        // Assert
        assertEquals("Updated Title", updatedTask.getTitle());
        assertEquals(TaskStatus.DONE, updatedTask.getStatus());
        assertEquals(persistedTask.getId(), updatedTask.getId());
    }

    // ==================== Tests pour les statuts ====================

    @Test
    @DisplayName("Doit sauvegarder les tâches avec différents statuts")
    void save_ShouldPersistTasksWithDifferentStatuses() {
        // Arrange
        Task todoTask = new Task();
        todoTask.setTitle("TODO Task");
        todoTask.setStatus(TaskStatus.TODO);
        todoTask.setUser(testUser);

        Task doneTask = new Task();
        doneTask.setTitle("DONE Task");
        doneTask.setStatus(TaskStatus.DONE);
        doneTask.setUser(testUser);

        // Act
        Task savedTodo = taskRepository.save(todoTask);
        Task savedDone = taskRepository.save(doneTask);

        // Assert
        assertEquals(TaskStatus.TODO, savedTodo.getStatus());
        assertEquals(TaskStatus.DONE, savedDone.getStatus());
    }
}
