package com.taskify.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.taskify.model.Task;
import com.taskify.model.TaskStatus;
import com.taskify.model.User;
import com.taskify.repository.TaskRepository;

/**
 * Tests unitaires pour TaskService.
 * Utilise Mockito pour simuler les dépendances.
 * Pattern AAA: Arrange - Act - Assert
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - TaskService")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private User testUser;
    private Task testTask;

    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() {
        // Arrange: Préparer les données de test
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setStatus(TaskStatus.TODO);
        testTask.setUser(testUser);
    }

    // ==================== Tests pour getAllTasksForUser() ====================

    @Test
    @DisplayName("getAllTasksForUser() - Doit retourner toutes les tâches de l'utilisateur")
    void getAllTasksForUser_ShouldReturnAllTasks() {
        // Arrange
        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");
        task2.setUser(testUser);
        
        List<Task> expectedTasks = Arrays.asList(testTask, task2);
        when(taskRepository.findByUser(testUser)).thenReturn(expectedTasks);

        // Act
        List<Task> result = taskService.getAllTasksForUser(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(taskRepository).findByUser(testUser);
    }

    @Test
    @DisplayName("getAllTasksForUser() - Doit retourner une liste vide si aucune tâche")
    void getAllTasksForUser_ShouldReturnEmptyList_WhenNoTasks() {
        // Arrange
        when(taskRepository.findByUser(testUser)).thenReturn(List.of());

        // Act
        List<Task> result = taskService.getAllTasksForUser(testUser);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(taskRepository).findByUser(testUser);
    }

    // ==================== Tests pour getTaskById() ====================

    @Test
    @DisplayName("getTaskById() - Doit retourner la tâche si elle appartient à l'utilisateur")
    void getTaskById_ShouldReturnTask_WhenTaskExistsAndBelongsToUser() {
        // Arrange
        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testTask));

        // Act
        Task result = taskService.getTaskById(1L, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(testTask.getId(), result.getId());
        assertEquals(testTask.getTitle(), result.getTitle());
        verify(taskRepository).findByIdAndUser(1L, testUser);
    }

    @Test
    @DisplayName("getTaskById() - Doit échouer si la tâche n'existe pas")
    void getTaskById_ShouldThrowException_WhenTaskNotFound() {
        // Arrange
        when(taskRepository.findByIdAndUser(99L, testUser)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> taskService.getTaskById(99L, testUser));
        
        assertEquals("Task not found or access denied", exception.getMessage());
        verify(taskRepository).findByIdAndUser(99L, testUser);
    }

    // ==================== Tests pour createTask() ====================

    @Test
    @DisplayName("createTask() - Doit créer une tâche avec tous les champs")
    void createTask_ShouldCreateTask_WithAllFields() {
        // Arrange
        String title = "New Task";
        String description = "New Description";
        TaskStatus status = TaskStatus.TODO;
        
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(1L);
            return task;
        });

        // Act
        Task result = taskService.createTask(title, description, status, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(description, result.getDescription());
        assertEquals(status, result.getStatus());
        assertEquals(testUser, result.getUser());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("createTask() - Doit définir le statut par défaut à TODO si null")
    void createTask_ShouldSetDefaultStatus_WhenStatusIsNull() {
        // Arrange
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(1L);
            return task;
        });

        // Act
        Task result = taskService.createTask("Title", "Desc", null, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(TaskStatus.TODO, result.getStatus());
        verify(taskRepository).save(any(Task.class));
    }

    // ==================== Tests pour updateTask() ====================

    @Test
    @DisplayName("updateTask() - Doit mettre à jour tous les champs de la tâche")
    void updateTask_ShouldUpdateAllFields() {
        // Arrange
        String newTitle = "Updated Title";
        String newDescription = "Updated Description";
        TaskStatus newStatus = TaskStatus.DONE;
        
        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Task result = taskService.updateTask(1L, newTitle, newDescription, newStatus, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(newTitle, result.getTitle());
        assertEquals(newDescription, result.getDescription());
        assertEquals(newStatus, result.getStatus());
        verify(taskRepository).findByIdAndUser(1L, testUser);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("updateTask() - Doit conserver les champs non modifiés")
    void updateTask_ShouldKeepExistingValues_WhenFieldsAreNull() {
        // Arrange
        String originalTitle = testTask.getTitle();
        String originalDescription = testTask.getDescription();
        TaskStatus originalStatus = testTask.getStatus();
        
        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Task result = taskService.updateTask(1L, null, null, null, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(originalTitle, result.getTitle());
        assertEquals(originalDescription, result.getDescription());
        assertEquals(originalStatus, result.getStatus());
    }

    @Test
    @DisplayName("updateTask() - Doit échouer si la tâche n'appartient pas à l'utilisateur")
    void updateTask_ShouldThrowException_WhenTaskNotBelongsToUser() {
        // Arrange
        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> taskService.updateTask(1L, "Title", "Desc", TaskStatus.DONE, testUser));
        
        assertEquals("Task not found or access denied", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    // ==================== Tests pour deleteTask() ====================

    @Test
    @DisplayName("deleteTask() - Doit supprimer la tâche avec succès")
    void deleteTask_ShouldDeleteTask_WhenTaskExists() {
        // Arrange
        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testTask));
        doNothing().when(taskRepository).delete(testTask);

        // Act
        taskService.deleteTask(1L, testUser);

        // Assert
        verify(taskRepository).findByIdAndUser(1L, testUser);
        verify(taskRepository).delete(testTask);
    }

    @Test
    @DisplayName("deleteTask() - Doit échouer si la tâche n'existe pas")
    void deleteTask_ShouldThrowException_WhenTaskNotFound() {
        // Arrange
        when(taskRepository.findByIdAndUser(99L, testUser)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> taskService.deleteTask(99L, testUser));
        
        assertEquals("Task not found or access denied", exception.getMessage());
        verify(taskRepository, never()).delete(any(Task.class));
    }
}
