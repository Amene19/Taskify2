package com.taskify.repository;

import java.time.LocalDateTime;
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

import com.taskify.model.Appointment;
import com.taskify.model.User;

/**
 * Tests du repository pour AppointmentRepository.
 * Utilise @DataJpaTest pour tester avec une base H2 en mémoire.
 * Pattern AAA: Arrange - Act - Assert
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests Repository - AppointmentRepository")
class AppointmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private User testUser;
    private User otherUser;
    private Appointment testAppointment;
    private LocalDateTime testDate;

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

        testDate = LocalDateTime.of(2026, 1, 20, 10, 0);

        testAppointment = new Appointment();
        testAppointment.setSubject("Test Appointment");
        testAppointment.setDate(testDate);
        testAppointment.setUser(testUser);
    }

    // ==================== Tests pour save() ====================

    @Test
    @DisplayName("save() - Doit sauvegarder un rendez-vous avec succès")
    void save_ShouldPersistAppointment() {
        // Act
        Appointment savedAppointment = appointmentRepository.save(testAppointment);

        // Assert
        assertNotNull(savedAppointment.getId());
        assertEquals(testAppointment.getSubject(), savedAppointment.getSubject());
        assertEquals(testAppointment.getDate(), savedAppointment.getDate());
        assertEquals(testUser.getId(), savedAppointment.getUser().getId());
    }

    @Test
    @DisplayName("save() - Doit générer un ID automatiquement")
    void save_ShouldGenerateId() {
        // Act
        Appointment savedAppointment = appointmentRepository.save(testAppointment);

        // Assert
        assertNotNull(savedAppointment.getId());
        assertTrue(savedAppointment.getId() > 0);
    }

    // ==================== Tests pour findByUser() ====================

    @Test
    @DisplayName("findByUser() - Doit retourner tous les rendez-vous de l'utilisateur")
    void findByUser_ShouldReturnAllAppointmentsForUser() {
        // Arrange
        Appointment appointment2 = new Appointment();
        appointment2.setSubject("Appointment 2");
        appointment2.setDate(LocalDateTime.of(2026, 2, 15, 14, 30));
        appointment2.setUser(testUser);

        entityManager.persistAndFlush(testAppointment);
        entityManager.persistAndFlush(appointment2);

        // Act
        List<Appointment> appointments = appointmentRepository.findByUser(testUser);

        // Assert
        assertEquals(2, appointments.size());
        assertTrue(appointments.stream().allMatch(a -> a.getUser().getId().equals(testUser.getId())));
    }

    @Test
    @DisplayName("findByUser() - Doit retourner une liste vide si l'utilisateur n'a pas de rendez-vous")
    void findByUser_ShouldReturnEmptyList_WhenUserHasNoAppointments() {
        // Act
        List<Appointment> appointments = appointmentRepository.findByUser(otherUser);

        // Assert
        assertTrue(appointments.isEmpty());
    }

    @Test
    @DisplayName("findByUser() - Ne doit pas retourner les rendez-vous d'autres utilisateurs")
    void findByUser_ShouldNotReturnOtherUsersAppointments() {
        // Arrange
        entityManager.persistAndFlush(testAppointment);

        Appointment otherAppointment = new Appointment();
        otherAppointment.setSubject("Other User Appointment");
        otherAppointment.setDate(LocalDateTime.of(2026, 3, 10, 16, 0));
        otherAppointment.setUser(otherUser);
        entityManager.persistAndFlush(otherAppointment);

        // Act
        List<Appointment> testUserAppointments = appointmentRepository.findByUser(testUser);
        List<Appointment> otherUserAppointments = appointmentRepository.findByUser(otherUser);

        // Assert
        assertEquals(1, testUserAppointments.size());
        assertEquals("Test Appointment", testUserAppointments.get(0).getSubject());
        assertEquals(1, otherUserAppointments.size());
        assertEquals("Other User Appointment", otherUserAppointments.get(0).getSubject());
    }

    // ==================== Tests pour findByIdAndUser() ====================

    @Test
    @DisplayName("findByIdAndUser() - Doit retourner le rendez-vous s'il appartient à l'utilisateur")
    void findByIdAndUser_ShouldReturnAppointment_WhenAppointmentBelongsToUser() {
        // Arrange
        Appointment persistedAppointment = entityManager.persistAndFlush(testAppointment);

        // Act
        Optional<Appointment> foundAppointment = appointmentRepository.findByIdAndUser(
            persistedAppointment.getId(), testUser);

        // Assert
        assertTrue(foundAppointment.isPresent());
        assertEquals(persistedAppointment.getId(), foundAppointment.get().getId());
    }

    @Test
    @DisplayName("findByIdAndUser() - Doit retourner Optional vide si le rendez-vous n'appartient pas à l'utilisateur")
    void findByIdAndUser_ShouldReturnEmpty_WhenAppointmentDoesNotBelongToUser() {
        // Arrange
        Appointment persistedAppointment = entityManager.persistAndFlush(testAppointment);

        // Act
        Optional<Appointment> foundAppointment = appointmentRepository.findByIdAndUser(
            persistedAppointment.getId(), otherUser);

        // Assert
        assertFalse(foundAppointment.isPresent());
    }

    @Test
    @DisplayName("findByIdAndUser() - Doit retourner Optional vide si le rendez-vous n'existe pas")
    void findByIdAndUser_ShouldReturnEmpty_WhenAppointmentDoesNotExist() {
        // Act
        Optional<Appointment> foundAppointment = appointmentRepository.findByIdAndUser(99999L, testUser);

        // Assert
        assertFalse(foundAppointment.isPresent());
    }

    // ==================== Tests pour findByUserId() ====================

    @Test
    @DisplayName("findByUserId() - Doit retourner tous les rendez-vous par ID utilisateur")
    void findByUserId_ShouldReturnAllAppointmentsForUserId() {
        // Arrange
        entityManager.persistAndFlush(testAppointment);

        Appointment appointment2 = new Appointment();
        appointment2.setSubject("Appointment 2");
        appointment2.setDate(LocalDateTime.of(2026, 2, 15, 14, 30));
        appointment2.setUser(testUser);
        entityManager.persistAndFlush(appointment2);

        // Act
        List<Appointment> appointments = appointmentRepository.findByUserId(testUser.getId());

        // Assert
        assertEquals(2, appointments.size());
    }

    // ==================== Tests pour delete() ====================

    @Test
    @DisplayName("delete() - Doit supprimer le rendez-vous")
    void delete_ShouldRemoveAppointment() {
        // Arrange
        Appointment persistedAppointment = entityManager.persistAndFlush(testAppointment);
        Long appointmentId = persistedAppointment.getId();

        // Act
        appointmentRepository.delete(persistedAppointment);
        entityManager.flush();

        // Assert
        Optional<Appointment> deletedAppointment = appointmentRepository.findById(appointmentId);
        assertFalse(deletedAppointment.isPresent());
    }

    // ==================== Tests pour update ====================

    @Test
    @DisplayName("save() - Doit mettre à jour un rendez-vous existant")
    void save_ShouldUpdateAppointment_WhenAppointmentExists() {
        // Arrange
        Appointment persistedAppointment = entityManager.persistAndFlush(testAppointment);
        LocalDateTime newDate = LocalDateTime.of(2026, 3, 20, 16, 0);

        // Act
        persistedAppointment.setSubject("Updated Subject");
        persistedAppointment.setDate(newDate);
        Appointment updatedAppointment = appointmentRepository.save(persistedAppointment);
        entityManager.flush();

        // Assert
        assertEquals("Updated Subject", updatedAppointment.getSubject());
        assertEquals(newDate, updatedAppointment.getDate());
        assertEquals(persistedAppointment.getId(), updatedAppointment.getId());
    }

    // ==================== Tests pour les dates ====================

    @Test
    @DisplayName("Doit sauvegarder les rendez-vous avec différentes dates")
    void save_ShouldPersistAppointmentsWithDifferentDates() {
        // Arrange
        LocalDateTime date1 = LocalDateTime.of(2026, 1, 15, 9, 0);
        LocalDateTime date2 = LocalDateTime.of(2026, 12, 31, 23, 59);

        Appointment appointment1 = new Appointment();
        appointment1.setSubject("January Appointment");
        appointment1.setDate(date1);
        appointment1.setUser(testUser);

        Appointment appointment2 = new Appointment();
        appointment2.setSubject("December Appointment");
        appointment2.setDate(date2);
        appointment2.setUser(testUser);

        // Act
        Appointment saved1 = appointmentRepository.save(appointment1);
        Appointment saved2 = appointmentRepository.save(appointment2);

        // Assert
        assertEquals(date1, saved1.getDate());
        assertEquals(date2, saved2.getDate());
    }
}
