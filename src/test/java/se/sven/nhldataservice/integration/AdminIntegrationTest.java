package se.sven.nhldataservice.integration;

import org.junit.jupiter.api.Test;
import se.sven.nhldataservice.model.User;
import se.sven.nhldataservice.model.enums.RoleName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminIntegrationTest extends BaseIntegrationTest {

    // === GET /api/v1/users ===

    @Test
    void shouldGetAllUsersAsAdmin() throws Exception {
        // Given
        User admin = createTestAdmin("testadmin", "testadmin@example.com", "admin123");
        createTestUser("user1", "user1@example.com", "password123");
        createTestUser("user2", "user2@example.com", "password123");
        String token = generateToken(admin);

        // When & Then
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4));
    }

    // === POST /api/v1/users ===

    @Test
    void shouldCreateNewUserAsAdmin() throws Exception {
        // Given
        User admin = createTestAdmin("testadmin", "testadmin@example.com", "admin123");
        String token = generateToken(admin);

        String createUserRequest = """
            {
                "username": "newuser",
                "email": "newuser@example.com",
                "password": "password123",
                "roles": ["USER"]
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", bearerToken(token))
                        .contentType(APPLICATION_JSON)
                        .content(createUserRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("USER"));

        // Verify user was created in database
        User created = userRepository.findByUsername("newuser").orElseThrow();
        assertThat(created.getEmail()).isEqualTo("newuser@example.com");
    }

    @Test
    void shouldNotAllowRegularUserToCreateUser() throws Exception {
        // Given
        User user = createTestUser("regularuser", "user@example.com", "password123");
        String token = generateToken(user);

        String createUserRequest = """
            {
                "username": "newuser",
                "email": "newuser@example.com",
                "password": "password123",
                "roles": ["USER"]
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", bearerToken(token))
                        .contentType(APPLICATION_JSON)
                        .content(createUserRequest))
                .andExpect(status().isForbidden());
    }

    // === GET /api/v1/users/{id} as Admin ===

    @Test
    void shouldAllowAdminToViewAnyUserProfile() throws Exception {
        // Given
        User admin = createTestAdmin("testadmin", "testadmin@example.com", "admin123");
        User otherUser = createTestUser("otheruser", "other@example.com", "password123");
        String token = generateToken(admin);

        // When & Then
        mockMvc.perform(get("/api/v1/users/" + otherUser.getId())
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("otheruser"));
    }

    // === PUT /api/v1/users/{id} as Admin ===

    @Test
    void shouldAllowAdminToUpdateAnyUser() throws Exception {
        // Given
        User admin = createTestAdmin("testadmin", "testadmin@example.com", "admin123");
        User otherUser = createTestUser("otheruser", "other@example.com", "password123");
        String token = generateToken(admin);

        String updateRequest = """
            {
                "username": "updateduser",
                "email": "updated@example.com"
            }
            """;

        // When & Then
        mockMvc.perform(put("/api/v1/users/" + otherUser.getId())
                        .header("Authorization", bearerToken(token))
                        .contentType(APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    // === DELETE /api/v1/users/{id} ===

    @Test
    void shouldAllowAdminToDeleteUser() throws Exception {
        // Given
        User admin = createTestAdmin("testadmin", "testadmin@example.com", "admin123");
        User userToDelete = createTestUser("todelete", "delete@example.com", "password123");
        String token = generateToken(admin);

        // When & Then
        mockMvc.perform(delete("/api/v1/users/" + userToDelete.getId())
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNoContent());

        // Verify user was deleted
        assertThat(userRepository.findById(userToDelete.getId())).isEmpty();
    }

    @Test
    void shouldNotAllowRegularUserToDeleteUser() throws Exception {
        // Given
        User user1 = createTestUser("user1", "user1@example.com", "password123");
        User user2 = createTestUser("user2", "user2@example.com", "password123");
        String token = generateToken(user1);

        // When & Then
        mockMvc.perform(delete("/api/v1/users/" + user2.getId())
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isForbidden());
    }

    // === PUT /api/v1/users/{id}/admin ===

    @Test
    void shouldAllowAdminToUpdateUserRoles() throws Exception {
        // Given
        User admin = createTestAdmin("testadmin", "testadmin@example.com", "admin123");
        User regularUser = createTestUser("regularuser", "user@example.com", "password123");
        String token = generateToken(admin);

        String adminUpdateRequest = """
            {
                "username": "regularuser",
                "email": "user@example.com",
                "enabled": true,
                "roles": ["ADMIN"]
            }
            """;

        // When & Then
        mockMvc.perform(put("/api/v1/users/" + regularUser.getId() + "/admin")
                        .header("Authorization", bearerToken(token))
                        .contentType(APPLICATION_JSON)
                        .content(adminUpdateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"));

        // Verify roles were updated
        User updated = userRepository.findById(regularUser.getId()).orElseThrow();
        assertThat(updated.hasRole(RoleName.ADMIN)).isTrue();
    }

    @Test
    void shouldAllowAdminToDisableUser() throws Exception {
        // Given
        User admin = createTestAdmin("testadmin", "testadmin@example.com", "admin123");
        User regularUser = createTestUser("regularuser", "user@example.com", "password123");
        String token = generateToken(admin);

        String adminUpdateRequest = """
            {
                "username": "regularuser",
                "email": "user@example.com",
                "enabled": false,
                "roles": ["USER"]
            }
            """;

        // When & Then
        mockMvc.perform(put("/api/v1/users/" + regularUser.getId() + "/admin")
                        .header("Authorization", bearerToken(token))
                        .contentType(APPLICATION_JSON)
                        .content(adminUpdateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));

        // Verify user was disabled
        User updated = userRepository.findById(regularUser.getId()).orElseThrow();
        assertThat(updated.isEnabled()).isFalse();
    }

    @Test
    void shouldNotAllowRegularUserToUpdateRoles() throws Exception {
        // Given
        User user1 = createTestUser("user1", "user1@example.com", "password123");
        User user2 = createTestUser("user2", "user2@example.com", "password123");
        String token = generateToken(user1);

        String adminUpdateRequest = """
            {
                "username": "user2",
                "email": "user2@example.com",
                "enabled": true,
                "roles": ["ADMIN"]
            }
            """;

        // When & Then
        mockMvc.perform(put("/api/v1/users/" + user2.getId() + "/admin")
                        .header("Authorization", bearerToken(token))
                        .contentType(APPLICATION_JSON)
                        .content(adminUpdateRequest))
                .andExpect(status().isForbidden());
    }

    // === Unauthenticated requests ===

    @Test
    void shouldReturn401ForUnauthenticatedGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401ForUnauthenticatedCreateUser() throws Exception {
        String createUserRequest = """
        {
            "username": "newuser",
            "email": "newuser@example.com",
            "password": "password123",
            "roles": ["USER"]
        }
        """;

        mockMvc.perform(post("/api/v1/users")
                        .contentType(APPLICATION_JSON)
                        .content(createUserRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401ForUnauthenticatedUpdateUser() throws Exception {
        User user = createTestUser("testuser", "test@example.com", "password123");

        String updateRequest = """
        {
            "username": "updated",
            "email": "updated@example.com"
        }
        """;

        mockMvc.perform(put("/api/v1/users/" + user.getId())
                        .contentType(APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401ForUnauthenticatedDeleteUser() throws Exception {
        User user = createTestUser("testuser", "test@example.com", "password123");

        mockMvc.perform(delete("/api/v1/users/" + user.getId()))
                .andExpect(status().isUnauthorized());
    }

// === Duplicate validation ===

    @Test
    void shouldRejectDuplicateUsername() throws Exception {
        // Given
        User admin = createTestAdmin("testadmin", "testadmin@example.com", "admin123");
        createTestUser("existinguser", "existing@example.com", "password123");
        String token = generateToken(admin);

        String createUserRequest = """
        {
            "username": "existinguser",
            "email": "newemail@example.com",
            "password": "password123",
            "roles": ["USER"]
        }
        """;

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", bearerToken(token))
                        .contentType(APPLICATION_JSON)
                        .content(createUserRequest))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectDuplicateEmail() throws Exception {
        // Given
        User admin = createTestAdmin("testadmin", "testadmin@example.com", "admin123");
        createTestUser("existinguser", "existing@example.com", "password123");
        String token = generateToken(admin);

        String createUserRequest = """
        {
            "username": "newuser",
            "email": "existing@example.com",
            "password": "password123",
            "roles": ["USER"]
        }
        """;

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", bearerToken(token))
                        .contentType(APPLICATION_JSON)
                        .content(createUserRequest))
                .andExpect(status().isConflict());
    }

// === Prevent deleting last admin ===

    @Test
    void shouldNotAllowDeletingLastAdmin() throws Exception {
        // Given - Only one admin exists (from DataLoader or test)
        User admin = createTestAdmin("testadmin", "testadmin@example.com", "admin123");
        String token = generateToken(admin);

        // When & Then - Try to delete self (last admin)
        mockMvc.perform(delete("/api/v1/users/" + admin.getId())
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isBadRequest()); // eller .isConflict() beroende på din implementation
    }
}