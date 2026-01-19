package se.sven.nhldataservice.integration;

import org.junit.jupiter.api.Test;
import se.sven.nhldataservice.model.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserIntegrationTest extends BaseIntegrationTest {

    // === GET /api/v1/users/me ===

    @Test
    void shouldGetCurrentUserProfile() throws Exception {
        // Given
        User user = createTestUser("testuser", "test@example.com", "password123");
        String token = generateToken(user);

        // When & Then
        mockMvc.perform(get("/api/v1/users/me").header("Authorization", bearerToken(token))).andExpect(status().isOk()).andExpect(jsonPath("$.username").value("testuser")).andExpect(jsonPath("$.email").value("test@example.com")).andExpect(jsonPath("$.roles[0]").value("USER"));
    }

    // === PUT /api/v1/users/me ===

    @Test
    void shouldUpdateOwnProfile() throws Exception {
        // Given
        User user = createTestUser("olduser", "old@example.com", "password123");
        String token = generateToken(user);

        String updateRequest = """
                {
                    "username": "newuser",
                    "email": "new@example.com"
                }
                """;

        // When
        mockMvc.perform(put("/api/v1/users/me").header("Authorization", bearerToken(token)).contentType(APPLICATION_JSON).content(updateRequest)).andExpect(status().isOk()).andExpect(jsonPath("$.username").value("newuser")).andExpect(jsonPath("$.email").value("new@example.com"));

        // Then
        User updated = userRepository.findByUsername("newuser").orElseThrow();
        assertThat(updated.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void shouldNotAllowUpdatingToExistingUsername() throws Exception {
        // Given
        createTestUser("existing", "existing@example.com", "password123");
        User otherUser = createTestUser("other", "other@example.com", "password123");
        String token = generateToken(otherUser);

        String updateRequest = """
                {
                    "username": "existing",
                    "email": "other@example.com"
                }
                """;

        // When & Then
        mockMvc.perform(put("/api/v1/users/me").header("Authorization", bearerToken(token)).contentType(APPLICATION_JSON).content(updateRequest)).andExpect(status().isConflict()).andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("already exists")));
    }

    @Test
    void shouldAllowKeepingSameUsername() throws Exception {
        // Given
        User user = createTestUser("testuser", "test@example.com", "password123");
        String token = generateToken(user);

        String updateRequest = """
                {
                    "username": "testuser",
                    "email": "newemail@example.com"
                }
                """;

        // When & Then
        mockMvc.perform(put("/api/v1/users/me").header("Authorization", bearerToken(token)).contentType(APPLICATION_JSON).content(updateRequest)).andExpect(status().isOk()).andExpect(jsonPath("$.username").value("testuser")).andExpect(jsonPath("$.email").value("newemail@example.com"));
    }

    // === PUT /api/v1/users/me/password ===

    @Test
    void shouldChangePasswordWithCorrectCurrentPassword() throws Exception {
        // Given
        User user = createTestUser("testuser", "test@example.com", "oldpassword");
        String token = generateToken(user);

        String changePasswordRequest = """
                {
                    "currentPassword": "oldpassword",
                    "newPassword": "newpassword123"
                }
                """;

        // When & Then
        mockMvc.perform(put("/api/v1/users/me/password").header("Authorization", bearerToken(token)).contentType(APPLICATION_JSON).content(changePasswordRequest)).andExpect(status().isNoContent());

        // Verify can login with new password
        String loginRequest = """
                {
                    "username": "testuser",
                    "password": "newpassword123"
                }
                """;

        mockMvc.perform(post("/auth/login").contentType(APPLICATION_JSON).content(loginRequest)).andExpect(status().isOk()).andExpect(jsonPath("$.token").exists());
    }

    @Test
    void shouldRejectPasswordChangeWithWrongCurrentPassword() throws Exception {
        // Given
        User user = createTestUser("testuser", "test@example.com", "correctpassword");
        String token = generateToken(user);

        String changePasswordRequest = """
                {
                    "currentPassword": "wrongpassword",
                    "newPassword": "newpassword123"
                }
                """;

        // When & Then
        mockMvc.perform(put("/api/v1/users/me/password").header("Authorization", bearerToken(token)).contentType(APPLICATION_JSON).content(changePasswordRequest)).andExpect(status().isBadRequest());
    }

    // === GET /api/v1/users/{id} ===

    @Test
    void shouldGetOwnProfileById() throws Exception {
        // Given
        User user = createTestUser("testuser", "test@example.com", "password123");
        String token = generateToken(user);

        // When & Then
        mockMvc.perform(get("/api/v1/users/" + user.getId()).header("Authorization", bearerToken(token))).andExpect(status().isOk()).andExpect(jsonPath("$.username").value("testuser")).andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void shouldAllowUserToViewOtherUserProfile() throws Exception {
        // Given
        User user1 = createTestUser("user1", "user1@example.com", "password123");
        User user2 = createTestUser("user2", "user2@example.com", "password123");
        String token = generateToken(user1);

        // When & Then
        mockMvc.perform(get("/api/v1/users/" + user2.getId()).header("Authorization", bearerToken(token))).andExpect(status().isOk()).andExpect(jsonPath("$.username").value("user2")).andExpect(jsonPath("$.email").value("user2@example.com"));
    }

    @Test
    void shouldGetAllUsersAsRegularUser() throws Exception {
        // Given
        User user = createTestUser("testuser", "test@example.com", "password123");
        createTestUser("user1", "user1@example.com", "password123");
        createTestUser("user2", "user2@example.com", "password123");
        String token = generateToken(user);

        // When & Then
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3))); // minst 3 användare
    }

    // === PUT /api/v1/users/{id} ===

    @Test
    void shouldUpdateOwnProfileById() throws Exception {
        // Given
        User user = createTestUser("olduser", "old@example.com", "password123");
        String token = generateToken(user);

        String updateRequest = """
                {
                    "username": "updateduser",
                    "email": "updated@example.com"
                }
                """;

        // When & Then
        mockMvc.perform(put("/api/v1/users/" + user.getId()).header("Authorization", bearerToken(token)).contentType(APPLICATION_JSON).content(updateRequest)).andExpect(status().isOk()).andExpect(jsonPath("$.username").value("updateduser")).andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    void shouldNotAllowUserToUpdateOtherUser() throws Exception {
        // Given
        User user1 = createTestUser("user1", "user1@example.com", "password123");
        User user2 = createTestUser("user2", "user2@example.com", "password123");
        String token = generateToken(user1);

        String updateRequest = """
                {
                    "username": "hacked",
                    "email": "hacked@example.com"
                }
                """;

        // When & Then
        mockMvc.perform(put("/api/v1/users/" + user2.getId()).header("Authorization", bearerToken(token)).contentType(APPLICATION_JSON).content(updateRequest)).andExpect(status().isForbidden());
    }

    // === Unauthenticated requests ===

    @Test
    void shouldRejectUnauthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")).andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/v1/users/me").contentType(APPLICATION_JSON).content("{}")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer invalid-token")).andExpect(status().isUnauthorized());
    }

// === Unauthenticated requests for public endpoints ===

    @Test
    void shouldReturn401ForUnauthenticatedGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401ForUnauthenticatedGetUserById() throws Exception {
        // Given
        User user = createTestUser("testuser", "test@example.com", "password123");

        // When & Then
        mockMvc.perform(get("/api/v1/users/" + user.getId()))
                .andExpect(status().isUnauthorized());
    }
}