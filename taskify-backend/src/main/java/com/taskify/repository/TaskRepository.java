package com.taskify.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taskify.model.Task;
import com.taskify.model.User;

/**
 * Repository interface for Task entity database operations.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Find all tasks belonging to a specific user.
     * @param user the owner of the tasks
     * @return list of tasks for the user
     */
    List<Task> findByUser(User user);

    /**
     * Find all tasks belonging to a user by user ID.
     * @param userId the ID of the user
     * @return list of tasks for the user
     */
    List<Task> findByUserId(Long userId);

    /**
     * Find a task by ID and user (for ownership verification).
     * @param id the task ID
     * @param user the owner
     * @return Optional containing the task if found and owned by user
     */
    Optional<Task> findByIdAndUser(Long id, User user);
}
