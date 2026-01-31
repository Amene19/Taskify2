package com.taskify.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.taskify.config.TestSecurityConfig;
import com.taskify.model.User;
import com.taskify.security.JwtFilter;
import com.taskify.security.JwtUtil;
import com.taskify.service.UserService;

/**
 * Tests des contrôleurs pour AuthController.
 * Utilise MockMvc pour tester les endpoints REST d'authentification.
 * Pattern AAA: Arrange - Act - Assert
 */
@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Tests MockMvc - AuthController")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @SuppressWarnings("unused")
    @MockBean
    private JwtFilter jwtFilter;

    @Value("${test.user.email}")
    private String testEmail;

    @Value("${test.user.password}")
    private String testPassword;

    @Value("${test.user.new.email}")
    private String newUserEmail;

    @Value("${test.user.existing.email}")
    private String existingUserEmail;

    private User testUser;

    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() {
        // Arrange: Préparer les données de test
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail(testEmail);
        testUser.setPassword("encodedPassword");
    }

    // ==================== Tests pour POST /api/auth/register ====================

    @Test
    @DisplayName("POST /api/auth/register - Doit créer un utilisateur avec succès")
    void register_ShouldCreateUser_WhenValidInput() throws Exception {
        // Arrange
        when(userService.register(newUserEmail, testPassword)).thenReturn(testUser);
        when(jwtUtil.generateToken(testEmail)).thenReturn("jwt-token-123");

        String requestBody = String.format("""
            {
                "email": "%s",
                "password": "%s"
            }
            """, newUserEmail, testPassword);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.message").value("Registration successful"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Doit retourner 400 si l'email existe déjà")
    void register_ShouldReturn400_WhenEmailAlreadyExists() throws Exception {
        // Arrange
        when(userService.register(existingUserEmail, testPassword))
            .thenThrow(new RuntimeException("Email already exists"));

        String requestBody = String.format("""
            {
                "email": "%s",
                "password": "%s"
            }
            """, existingUserEmail, testPassword);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email already exists"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Doit retourner 400 si l'email est invalide")
    void register_ShouldReturn400_WhenEmailIsInvalid() throws Exception {
        // Arrange
        String requestBody = """
            {
                "email": "invalid-email",
                "password": "password123"
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register - Doit retourner 400 si le mot de passe est manquant")
    void register_ShouldReturn400_WhenPasswordIsMissing() throws Exception {
        // Arrange
        String requestBody = String.format("""
            {
                "email": "%s"
            }
            """, testEmail);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    // ==================== Tests pour POST /api/auth/login ====================

    @Test
    @DisplayName("POST /api/auth/login - Doit authentifier l'utilisateur avec succès")
    void login_ShouldAuthenticateUser_WhenValidCredentials() throws Exception {
        // Arrange
        when(userService.authenticate(testEmail, testPassword)).thenReturn(testUser);
        when(jwtUtil.generateToken(testEmail)).thenReturn("jwt-token-456");

        String requestBody = String.format("""
            {
                "email": "%s",
                "password": "%s"
            }
            """, testEmail, testPassword);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-456"))
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Doit retourner 401 si les identifiants sont invalides")
    void login_ShouldReturn401_WhenInvalidCredentials() throws Exception {
        // Arrange
        when(userService.authenticate(testEmail, "wrongpassword"))
            .thenThrow(new RuntimeException("Invalid email or password"));

        String requestBody = String.format("""
            {
                "email": "%s",
                "password": "wrongpassword"
            }
            """, testEmail);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Doit retourner 400 si l'email est manquant")
    void login_ShouldReturn400_WhenEmailIsMissing() throws Exception {
        // Arrange
        String requestBody = String.format("""
            {
                "password": "%s"
            }
            """, testPassword);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    // ==================== Tests pour POST /api/auth/logout ====================

    @Test
    @DisplayName("POST /api/auth/logout - Doit retourner un message de succès")
    void logout_ShouldReturnSuccessMessage() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successful"))
                .andExpect(jsonPath("$.instruction").value("Token should be discarded by client"));
    }
}
