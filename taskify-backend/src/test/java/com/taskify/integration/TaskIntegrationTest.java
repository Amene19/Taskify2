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
import com.taskify.repository.TaskRepository;
import com.taskify.repository.UserRepository;

/**
 * Tests d'intégration pour les tâches.
 * Teste le flux complet: Controller → Service → Repository → Database
 * Utilise @SpringBootTest pour charger le contexte complet de l'application.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Tests d'intégration - Task CRUD")
class TaskIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    private String jwtToken;
    private Long createdTaskId;

    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() throws Exception {
        // Nettoyer la base de données avant chaque test
        taskRepository.deleteAll();
        userRepository.deleteAll();

        // Créer un utilisateur et obtenir le token JWT
        String registerRequest = """
            {
                "email": "integration@test.com",
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
    @DisplayName("Scénario complet: Créer → Lire → Modifier → Supprimer une tâche")
    void fullCrudScenario() throws Exception {
        // ========== ÉTAPE 1: Créer une tâche ==========
        String createRequest = """
            {
                "title": "Integration Test Task",
                "description": "Task created during integration test",
                "status": "TODO"
            }
            """;

        MvcResult createResult = mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration Test Task"))
                .andExpect(jsonPath("$.description").value("Task created during integration test"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andReturn();

        // Extraire l'ID de la tâche créée
        String createResponse = createResult.getResponse().getContentAsString();
        JsonNode taskNode = objectMapper.readTree(createResponse);
        createdTaskId = taskNode.get("id").asLong();

        // ========== ÉTAPE 2: Lire toutes les tâches ==========
        mockMvc.perform(get("/api/tasks")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Integration Test Task"));

        // ========== ÉTAPE 3: Lire une tâche par ID ==========
        mockMvc.perform(get("/api/tasks/" + createdTaskId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdTaskId))
                .andExpect(jsonPath("$.title").value("Integration Test Task"));

        // ========== ÉTAPE 4: Mettre à jour la tâche ==========
        String updateRequest = """
            {
                "title": "Updated Integration Task",
                "description": "Updated description",
                "status": "DONE"
            }
            """;

        mockMvc.perform(put("/api/tasks/" + createdTaskId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Integration Task"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.status").value("DONE"));

        // ========== ÉTAPE 5: Supprimer la tâche ==========
        mockMvc.perform(delete("/api/tasks/" + createdTaskId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // ========== ÉTAPE 6: Vérifier que la tâche est supprimée ==========
        mockMvc.perform(get("/api/tasks/" + createdTaskId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Vérifier que la liste est vide
        mockMvc.perform(get("/api/tasks")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @Order(2)
    @DisplayName("Doit retourner 401 sans authentification")
    void shouldReturn401WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    @DisplayName("Doit créer plusieurs tâches et les lister")
    void shouldCreateMultipleTasksAndListThem() throws Exception {
        // Créer 3 tâches
        for (int i = 1; i <= 3; i++) {
            String createRequest = String.format("""
                {
                    "title": "Task %d",
                    "description": "Description %d",
                    "status": "TODO"
                }
                """, i, i);

            mockMvc.perform(post("/api/tasks")
                    .header("Authorization", "Bearer " + jwtToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createRequest))
                    .andExpect(status().isCreated());
        }

        // Vérifier que les 3 tâches sont créées
        mockMvc.perform(get("/api/tasks")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @Order(4)
    @DisplayName("Doit retourner 404 pour une tâche inexistante")
    void shouldReturn404ForNonExistentTask() throws Exception {
        mockMvc.perform(get("/api/tasks/99999")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(5)
    @DisplayName("Doit valider les champs requis lors de la création")
    void shouldValidateRequiredFieldsOnCreate() throws Exception {
        // Tenter de créer une tâche sans titre
        String invalidRequest = """
            {
                "description": "Description without title"
            }
            """;

        mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }
}
