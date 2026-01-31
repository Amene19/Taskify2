package com.taskify.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taskify.model.Appointment;
import com.taskify.model.User;
import com.taskify.repository.AppointmentRepository;

/**
 * Service class for Appointment-related business logic.
 */
@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    /**
     * Get all appointments for a user.
     * @param user the owner of the appointments
     * @return list of user's appointments
     */
    public List<Appointment> getAllAppointmentsForUser(User user) {
        return appointmentRepository.findByUser(user);
    }

    /**
     * Get an appointment by ID, verifying ownership.
     * @param id appointment ID
     * @param user the owner
     * @return the appointment
     * @throws RuntimeException if appointment not found or not owned by user
     */
    public Appointment getAppointmentById(Long id, User user) {
        return appointmentRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new RuntimeException("Appointment not found or access denied"));
    }

    /**
     * Create a new appointment for a user.
     * @param subject appointment subject
     * @param date appointment date and time
     * @param user appointment owner
     * @return the created appointment
     */
    public Appointment createAppointment(String subject, LocalDateTime date, User user) {
        Appointment appointment = new Appointment();
        appointment.setSubject(subject);
        appointment.setDate(date);
        appointment.setUser(user);
        
        return appointmentRepository.save(appointment);
    }

    /**
     * Update an existing appointment.
     * @param id appointment ID
     * @param subject new subject
     * @param date new date
     * @param user appointment owner (for ownership verification)
     * @return the updated appointment
     */
    public Appointment updateAppointment(Long id, String subject, LocalDateTime date, User user) {
        Appointment appointment = getAppointmentById(id, user);
        
        if (subject != null) {
            appointment.setSubject(subject);
        }
        if (date != null) {
            appointment.setDate(date);
        }
        
        return appointmentRepository.save(appointment);
    }

    /**
     * Delete an appointment.
     * @param id appointment ID
     * @param user appointment owner (for ownership verification)
     */
    public void deleteAppointment(Long id, User user) {
        Appointment appointment = getAppointmentById(id, user);
        appointmentRepository.delete(appointment);
    }
}
