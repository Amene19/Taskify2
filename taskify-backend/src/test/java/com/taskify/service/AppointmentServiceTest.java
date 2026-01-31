package com.taskify.service;

import java.time.LocalDateTime;
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

import com.taskify.model.Appointment;
import com.taskify.model.User;
import com.taskify.repository.AppointmentRepository;

/**
 * Tests unitaires pour AppointmentService.
 * Utilise Mockito pour simuler les dépendances.
 * Pattern AAA: Arrange - Act - Assert
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - AppointmentService")
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private User testUser;
    private Appointment testAppointment;
    private LocalDateTime testDate;

    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() {
        // Arrange: Préparer les données de test
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testDate = LocalDateTime.of(2026, 1, 20, 10, 0);

        testAppointment = new Appointment();
        testAppointment.setId(1L);
        testAppointment.setSubject("Test Appointment");
        testAppointment.setDate(testDate);
        testAppointment.setUser(testUser);
    }

    // ==================== Tests pour getAllAppointmentsForUser() ====================

    @Test
    @DisplayName("getAllAppointmentsForUser() - Doit retourner tous les rendez-vous de l'utilisateur")
    void getAllAppointmentsForUser_ShouldReturnAllAppointments() {
        // Arrange
        Appointment appointment2 = new Appointment();
        appointment2.setId(2L);
        appointment2.setSubject("Appointment 2");
        appointment2.setUser(testUser);
        
        List<Appointment> expectedAppointments = Arrays.asList(testAppointment, appointment2);
        when(appointmentRepository.findByUser(testUser)).thenReturn(expectedAppointments);

        // Act
        List<Appointment> result = appointmentService.getAllAppointmentsForUser(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(appointmentRepository).findByUser(testUser);
    }

    @Test
    @DisplayName("getAllAppointmentsForUser() - Doit retourner une liste vide si aucun rendez-vous")
    void getAllAppointmentsForUser_ShouldReturnEmptyList_WhenNoAppointments() {
        // Arrange
        when(appointmentRepository.findByUser(testUser)).thenReturn(List.of());

        // Act
        List<Appointment> result = appointmentService.getAllAppointmentsForUser(testUser);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(appointmentRepository).findByUser(testUser);
    }

    // ==================== Tests pour getAppointmentById() ====================

    @Test
    @DisplayName("getAppointmentById() - Doit retourner le rendez-vous s'il appartient à l'utilisateur")
    void getAppointmentById_ShouldReturnAppointment_WhenAppointmentExistsAndBelongsToUser() {
        // Arrange
        when(appointmentRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testAppointment));

        // Act
        Appointment result = appointmentService.getAppointmentById(1L, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(testAppointment.getId(), result.getId());
        assertEquals(testAppointment.getSubject(), result.getSubject());
        verify(appointmentRepository).findByIdAndUser(1L, testUser);
    }

    @Test
    @DisplayName("getAppointmentById() - Doit échouer si le rendez-vous n'existe pas")
    void getAppointmentById_ShouldThrowException_WhenAppointmentNotFound() {
        // Arrange
        when(appointmentRepository.findByIdAndUser(99L, testUser)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> appointmentService.getAppointmentById(99L, testUser));
        
        assertEquals("Appointment not found or access denied", exception.getMessage());
        verify(appointmentRepository).findByIdAndUser(99L, testUser);
    }

    // ==================== Tests pour createAppointment() ====================

    @Test
    @DisplayName("createAppointment() - Doit créer un rendez-vous avec tous les champs")
    void createAppointment_ShouldCreateAppointment_WithAllFields() {
        // Arrange
        String subject = "New Appointment";
        LocalDateTime date = LocalDateTime.of(2026, 2, 15, 14, 30);
        
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment appointment = invocation.getArgument(0);
            appointment.setId(1L);
            return appointment;
        });

        // Act
        Appointment result = appointmentService.createAppointment(subject, date, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(subject, result.getSubject());
        assertEquals(date, result.getDate());
        assertEquals(testUser, result.getUser());
        verify(appointmentRepository).save(any(Appointment.class));
    }

    // ==================== Tests pour updateAppointment() ====================

    @Test
    @DisplayName("updateAppointment() - Doit mettre à jour tous les champs du rendez-vous")
    void updateAppointment_ShouldUpdateAllFields() {
        // Arrange
        String newSubject = "Updated Subject";
        LocalDateTime newDate = LocalDateTime.of(2026, 3, 10, 16, 0);
        
        when(appointmentRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Appointment result = appointmentService.updateAppointment(1L, newSubject, newDate, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(newSubject, result.getSubject());
        assertEquals(newDate, result.getDate());
        verify(appointmentRepository).findByIdAndUser(1L, testUser);
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("updateAppointment() - Doit conserver les champs non modifiés")
    void updateAppointment_ShouldKeepExistingValues_WhenFieldsAreNull() {
        // Arrange
        String originalSubject = testAppointment.getSubject();
        LocalDateTime originalDate = testAppointment.getDate();
        
        when(appointmentRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Appointment result = appointmentService.updateAppointment(1L, null, null, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(originalSubject, result.getSubject());
        assertEquals(originalDate, result.getDate());
    }

    @Test
    @DisplayName("updateAppointment() - Doit échouer si le rendez-vous n'appartient pas à l'utilisateur")
    void updateAppointment_ShouldThrowException_WhenAppointmentNotBelongsToUser() {
        // Arrange
        when(appointmentRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> appointmentService.updateAppointment(1L, "Subject", LocalDateTime.now(), testUser));
        
        assertEquals("Appointment not found or access denied", exception.getMessage());
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    // ==================== Tests pour deleteAppointment() ====================

    @Test
    @DisplayName("deleteAppointment() - Doit supprimer le rendez-vous avec succès")
    void deleteAppointment_ShouldDeleteAppointment_WhenAppointmentExists() {
        // Arrange
        when(appointmentRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testAppointment));
        doNothing().when(appointmentRepository).delete(testAppointment);

        // Act
        appointmentService.deleteAppointment(1L, testUser);

        // Assert
        verify(appointmentRepository).findByIdAndUser(1L, testUser);
        verify(appointmentRepository).delete(testAppointment);
    }

    @Test
    @DisplayName("deleteAppointment() - Doit échouer si le rendez-vous n'existe pas")
    void deleteAppointment_ShouldThrowException_WhenAppointmentNotFound() {
        // Arrange
        when(appointmentRepository.findByIdAndUser(99L, testUser)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> appointmentService.deleteAppointment(99L, testUser));
        
        assertEquals("Appointment not found or access denied", exception.getMessage());
        verify(appointmentRepository, never()).delete(any(Appointment.class));
    }
}
