package se.sven.nhldataservice.integration;

import org.junit.jupiter.api.Test;
import se.sven.nhldataservice.model.User;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldLoginWithValidCredentials() throws Exception {
        // Given
        createTestUser("testuser", "test@example.com", "password123");

        String loginRequest = """
            {
                "username": "testuser",
                "password": "password123"
            }
            """;

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void shouldRejectLoginWithInvalidPassword() throws Exception {
        // Given
        createTestUser("testuser", "test@example.com", "password123");

        String loginRequest = """
            {
                "username": "testuser",
                "password": "wrongpassword"
            }
            """;

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectLoginForNonExistentUser() throws Exception {
        // Given
        String loginRequest = """
            {
                "username": "nonexistent",
                "password": "password123"
            }
            """;

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRegisterNewUser() throws Exception {
        // Given
        String registerRequest = """
        {
            "username": "newuser",
            "email": "newuser@example.com",
            "password": "password123"
        }
        """;

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("USER"));

        String loginRequest = """
        {
            "username": "newuser",
            "password": "password123"
        }
        """;

        mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void shouldRejectRegistrationWithExistingUsername() throws Exception {
        // Given
        createTestUser("existinguser", "existing@example.com", "password123");

        String registerRequest = """
        {
            "username": "existinguser",
            "email": "newemail@example.com",
            "password": "password123"
        }
        """;

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("already exists")));
    }

    @Test
    void shouldRejectRegistrationWithExistingEmail() throws Exception {
        // Given
        createTestUser("existinguser", "existing@example.com", "password123");

        String registerRequest = """
        {
            "username": "newusername",
            "email": "existing@example.com",
            "password": "password123"
        }
        """;

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("already exists")));
    }

    @Test
    void shouldRejectLoginForDisabledUser() throws Exception {
        // Given
        User disabledUser = createTestUser("disableduser", "disabled@example.com", "password123");
        disabledUser.setEnabled(false);
        userRepository.save(disabledUser);

        String loginRequest = """
        {
            "username": "disableduser",
            "password": "password123"
        }
        """;

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAcceptValidToken() throws Exception {
        // Given
        User user = createTestUser("testuser", "test@example.com", "password123");
        String token = generateToken(user);

        // When & Then - Use token to access protected endpoint
        mockMvc.perform(get("/api/v1/users/me")  // ← GET istället för POST!
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }
    @Test
    void shouldRejectEmptyUsername() throws Exception {
        // Given
        String loginRequest = """
        {
            "username": "",
            "password": "password123"
        }
        """;

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectEmptyPassword() throws Exception {
        // Given
        createTestUser("testuser", "test@example.com", "password123");

        String loginRequest = """
        {
            "username": "testuser",
            "password": ""
        }
        """;

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectMalformedLoginRequest() throws Exception {
        // Given - Missing password field
        String loginRequest = """
        {
            "username": "testuser"
        }
        """;

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectMalformedRegisterRequest() throws Exception {
        // Given - Missing required fields
        String registerRequest = """
        {
            "username": "newuser"
        }
        """;

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isBadRequest());
    }
}