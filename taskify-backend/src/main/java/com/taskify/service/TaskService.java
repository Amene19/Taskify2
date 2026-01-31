package com.taskify.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taskify.model.Task;
import com.taskify.model.TaskStatus;
import com.taskify.model.User;
import com.taskify.repository.TaskRepository;

/**
 * Service class for Task-related business logic.
 */
@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    /**
     * Get all tasks for a user.
     * @param user the owner of the tasks
     * @return list of user's tasks
     */
    public List<Task> getAllTasksForUser(User user) {
        return taskRepository.findByUser(user);
    }

    /**
     * Get a task by ID, verifying ownership.
     * @param id task ID
     * @param user the owner
     * @return the task
     * @throws RuntimeException if task not found or not owned by user
     */
    public Task getTaskById(Long id, User user) {
        return taskRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new RuntimeException("Task not found or access denied"));
    }

    /**
     * Create a new task for a user.
     * @param title task title
     * @param description task description
     * @param status task status
     * @param user task owner
     * @return the created task
     */
    public Task createTask(String title, String description, TaskStatus status, User user) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status != null ? status : TaskStatus.TODO);
        task.setUser(user);
        
        return taskRepository.save(task);
    }

    /**
     * Update an existing task.
     * @param id task ID
     * @param title new title
     * @param description new description
     * @param status new status
     * @param user task owner (for ownership verification)
     * @return the updated task
     */
    public Task updateTask(Long id, String title, String description, TaskStatus status, User user) {
        Task task = getTaskById(id, user);
        
        if (title != null) {
            task.setTitle(title);
        }
        if (description != null) {
            task.setDescription(description);
        }
        if (status != null) {
            task.setStatus(status);
        }
        
        return taskRepository.save(task);
    }

    /**
     * Delete a task.
     * @param id task ID
     * @param user task owner (for ownership verification)
     */
    public void deleteTask(Long id, User user) {
        Task task = getTaskById(id, user);
        taskRepository.delete(task);
    }
}
