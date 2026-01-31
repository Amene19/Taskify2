package com.taskify.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taskify.model.Appointment;
import com.taskify.model.User;

/**
 * Repository interface for Appointment entity database operations.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Find all appointments belonging to a specific user.
     * @param user the owner of the appointments
     * @return list of appointments for the user
     */
    List<Appointment> findByUser(User user);

    /**
     * Find all appointments belonging to a user by user ID.
     * @param userId the ID of the user
     * @return list of appointments for the user
     */
    List<Appointment> findByUserId(Long userId);

    /**
     * Find an appointment by ID and user (for ownership verification).
     * @param id the appointment ID
     * @param user the owner
     * @return Optional containing the appointment if found and owned by user
     */
    Optional<Appointment> findByIdAndUser(Long id, User user);
}
