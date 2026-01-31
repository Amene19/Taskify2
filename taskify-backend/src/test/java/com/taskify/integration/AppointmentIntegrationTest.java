package com.taskify.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskify.repository.AppointmentRepository;
import com.taskify.repository.UserRepository;

/**
 * Tests d'intégration pour les rendez-vous.
 * Teste le flux complet: Controller → Service → Repository → Database
 * Utilise @SpringBootTest pour charger le contexte complet de l'application.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Tests d'intégration - Appointment CRUD")
class AppointmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private String jwtToken;
    private Long createdAppointmentId;

    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() throws Exception {
        // Nettoyer la base de données avant chaque test
        appointmentRepository.deleteAll();
        userRepository.deleteAll();

        // Créer un utilisateur et obtenir le token JWT
        String registerRequest = """
            {
                "email": "appointment-test@test.com",
                "password": "password123"
            }
            """;

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerRequest))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        jwtToken = jsonNode.get("token").asText();
    }

    // ==================== Scénario CRUD complet ====================

    @Test
    @Order(1)
    @DisplayName("Scénario complet: Créer → Lire → Modifier → Supprimer un rendez-vous")
    void fullCrudScenario() throws Exception {
        // ========== ÉTAPE 1: Créer un rendez-vous ==========
        String createRequest = """
            {
                "subject": "Integration Test Appointment",
                "date": "2026-02-15T10:00:00"
            }
            """;

        MvcResult createResult = mockMvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subject").value("Integration Test Appointment"))
                .andReturn();

        // Extraire l'ID du rendez-vous créé
        String createResponse = createResult.getResponse().getContentAsString();
        JsonNode appointmentNode = objectMapper.readTree(createResponse);
        createdAppointmentId = appointmentNode.get("id").asLong();

        // ========== ÉTAPE 2: Lire tous les rendez-vous ==========
        mockMvc.perform(get("/api/appointments")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].subject").value("Integration Test Appointment"));

        // ========== ÉTAPE 3: Lire un rendez-vous par ID ==========
        mockMvc.perform(get("/api/appointments/" + createdAppointmentId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdAppointmentId))
                .andExpect(jsonPath("$.subject").value("Integration Test Appointment"));

        // ========== ÉTAPE 4: Mettre à jour le rendez-vous ==========
        String updateRequest = """
            {
                "subject": "Updated Integration Appointment",
                "date": "2026-03-20T14:30:00"
            }
            """;

        mockMvc.perform(put("/api/appointments/" + createdAppointmentId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("Updated Integration Appointment"));

        // ========== ÉTAPE 5: Supprimer le rendez-vous ==========
        mockMvc.perform(delete("/api/appointments/" + createdAppointmentId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // ========== ÉTAPE 6: Vérifier que le rendez-vous est supprimé ==========
        mockMvc.perform(get("/api/appointments/" + createdAppointmentId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Vérifier que la liste est vide
        mockMvc.perform(get("/api/appointments")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @Order(2)
    @DisplayName("Doit retourner 401 sans authentification")
    void shouldReturn401WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    @DisplayName("Doit créer plusieurs rendez-vous et les lister")
    void shouldCreateMultipleAppointmentsAndListThem() throws Exception {
        // Créer 3 rendez-vous
        String[] subjects = {"Meeting 1", "Meeting 2", "Meeting 3"};
        String[] dates = {"2026-02-10T09:00:00", "2026-02-15T11:00:00", "2026-02-20T14:00:00"};

        for (int i = 0; i < 3; i++) {
            String createRequest = String.format("""
                {
                    "subject": "%s",
                    "date": "%s"
                }
                """, subjects[i], dates[i]);

            mockMvc.perform(post("/api/appointments")
                    .header("Authorization", "Bearer " + jwtToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createRequest))
                    .andExpect(status().isCreated());
        }

        // Vérifier que les 3 rendez-vous sont créés
        mockMvc.perform(get("/api/appointments")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @Order(4)
    @DisplayName("Doit retourner 404 pour un rendez-vous inexistant")
    void shouldReturn404ForNonExistentAppointment() throws Exception {
        mockMvc.perform(get("/api/appointments/99999")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(5)
    @DisplayName("Doit valider les champs requis lors de la création")
    void shouldValidateRequiredFieldsOnCreate() throws Exception {
        // Tenter de créer un rendez-vous sans sujet
        String invalidRequest = """
            {
                "date": "2026-02-15T10:00:00"
            }
            """;

        mockMvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    @DisplayName("Les rendez-vous sont isolés par utilisateur")
    void appointmentsShouldBeIsolatedPerUser() throws Exception {
        // Créer un rendez-vous avec le premier utilisateur
        String createRequest = """
            {
                "subject": "User 1 Appointment",
                "date": "2026-02-15T10:00:00"
            }
            """;

        mockMvc.perform(post("/api/appointments")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest))
                .andExpect(status().isCreated());

        // Créer un second utilisateur
        String registerRequest = """
            {
                "email": "user2@test.com",
                "password": "password123"
            }
            """;

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerRequest))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        String user2Token = jsonNode.get("token").asText();

        // Vérifier que le second utilisateur ne voit pas les rendez-vous du premier
        mockMvc.perform(get("/api/appointments")
                .header("Authorization", "Bearer " + user2Token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
