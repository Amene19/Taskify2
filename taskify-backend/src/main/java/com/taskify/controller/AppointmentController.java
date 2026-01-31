package com.taskify.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskify.model.Appointment;
import com.taskify.model.User;
import com.taskify.service.AppointmentService;
import com.taskify.service.UserService;

import io.micrometer.core.instrument.Counter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Appointment management controller for CRUD operations.
 */
@RestController
@RequestMapping("/api/appointments")
@Tag(name = "Appointments", description = "Appointment management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserService userService;

    @Autowired
    private Counter appointmentCreatedCounter;

    /**
     * Get current authenticated user from security context.
     */
    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userService.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Get all appointments for the authenticated user.
     */
    @GetMapping
    @Operation(summary = "Get all appointments", description = "Retrieves all appointments for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Appointments retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments(Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<Appointment> appointments = appointmentService.getAllAppointmentsForUser(user);
        List<AppointmentResponse> response = appointments.stream()
            .map(AppointmentResponse::fromAppointment)
            .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific appointment by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get appointment by ID", description = "Retrieves a specific appointment by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Appointment retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Appointment not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<?> getAppointmentById(@PathVariable Long id, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            Appointment appointment = appointmentService.getAppointmentById(id, user);
            return ResponseEntity.ok(AppointmentResponse.fromAppointment(appointment));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Create a new appointment.
     */
    @PostMapping
    @Operation(summary = "Create a new appointment", description = "Creates a new appointment for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Appointment created successfully",
            content = @Content(schema = @Schema(implementation = AppointmentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<?> createAppointment(@Valid @RequestBody AppointmentRequest request, 
                                                Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            Appointment appointment = appointmentService.createAppointment(
                request.getSubject(),
                request.getDate(),
                user
            );
            appointmentCreatedCounter.increment();
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(AppointmentResponse.fromAppointment(appointment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Update an existing appointment.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an appointment", description = "Updates an existing appointment by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Appointment updated successfully",
            content = @Content(schema = @Schema(implementation = AppointmentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Appointment not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<?> updateAppointment(@PathVariable Long id,
                                                @Valid @RequestBody AppointmentRequest request,
                                                Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            Appointment appointment = appointmentService.updateAppointment(
                id,
                request.getSubject(),
                request.getDate(),
                user
            );
            return ResponseEntity.ok(AppointmentResponse.fromAppointment(appointment));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Delete an appointment.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an appointment", description = "Deletes an appointment by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Appointment deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Appointment not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id, Authentication authentication) {
        try {
            User user = getCurrentUser(authentication);
            appointmentService.deleteAppointment(id, user);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        }
    }

    // ==================== DTOs ====================================

    public static class AppointmentRequest {
        @NotBlank(message = "Subject is required")
        private String subject;

        @NotNull(message = "Date is required")
        private LocalDateTime date;

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public LocalDateTime getDate() { return date; }
        public void setDate(LocalDateTime date) { this.date = date; }
    }

    public static class AppointmentResponse {
        private Long id;
        private String subject;
        private LocalDateTime date;

        public static AppointmentResponse fromAppointment(Appointment appointment) {
            AppointmentResponse response = new AppointmentResponse();
            response.setId(appointment.getId());
            response.setSubject(appointment.getSubject());
            response.setDate(appointment.getDate());
            return response;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public LocalDateTime getDate() { return date; }
        public void setDate(LocalDateTime date) { this.date = date; }
    }

    public static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
