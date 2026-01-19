package se.sven.nhldataservice.integration;

import org.junit.jupiter.api.Test;
import se.sven.nhldataservice.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GameIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldGetGamesWithValidToken() throws Exception {
        // Given
        User user = createTestUser("testuser", "test@example.com", "password123");
        String token = generateToken(user);
        String today = LocalDate.now().toString();

        // When & Then
        mockMvc.perform(get("/api/v1/games/" + today)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn204WhenNoGamesFound() throws Exception {
        // Given
        User user = createTestUser("testuser", "test@example.com", "password123");
        String token = generateToken(user);
        String farFutureDate = LocalDate.now().plusYears(10).toString();

        // When & Then
        mockMvc.perform(get("/api/v1/games/" + farFutureDate)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectInvalidDateFormat() throws Exception {
        // Given
        User user = createTestUser("testuser", "test@example.com", "password123");
        String token = generateToken(user);

        // When & Then
        mockMvc.perform(get("/api/v1/games/invalid-date")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRequireAuthentication() throws Exception {
        // Given
        String today = LocalDate.now().toString();

        // When & Then
        mockMvc.perform(get("/api/v1/games/" + today))
                .andExpect(status().isUnauthorized());
    }

// === Test correct JSON structure ===

    @Test
    void shouldReturnCorrectGameFormat() throws Exception {
        // Given
        User user = createTestUser("testuser", "test@example.com", "password123");
        String token = generateToken(user);
        String dateWithGames = "2026-01-13"; // Datum där vi VET att det finns games

        // When & Then
        mockMvc.perform(get("/api/v1/games/" + dateWithGames)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].season").exists())
                .andExpect(jsonPath("$[0].gameDate").value(dateWithGames))
                .andExpect(jsonPath("$[0].awayTeam").exists())
                .andExpect(jsonPath("$[0].awayTeam.id").exists())
                .andExpect(jsonPath("$[0].awayTeam.abbrev").exists())
                .andExpect(jsonPath("$[0].awayTeam.score").exists())
                .andExpect(jsonPath("$[0].homeTeam").exists())
                .andExpect(jsonPath("$[0].homeTeam.id").exists())
                .andExpect(jsonPath("$[0].homeTeam.abbrev").exists())
                .andExpect(jsonPath("$[0].homeTeam.score").exists())
                .andExpect(jsonPath("$[0].venue").exists())
                .andExpect(jsonPath("$[0].gameState").exists());
    }

// === Test past dates ===

    @Test
    void shouldAcceptDatesInPast() throws Exception {
        // Given
        User user = createTestUser("testuser", "test@example.com", "password123");
        String token = generateToken(user);
        String pastDate = "2025-12-01"; // Datum i det förflutna

        // When & Then
        mockMvc.perform(get("/api/v1/games/" + pastDate)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk()) // eller .isNoContent() om inga games
                .andExpect(jsonPath("$").isArray());
    }

// === Test far future dates ===

    @Test
    void shouldReturnNoContentForFarFutureDates() throws Exception {
        // Given
        User user = createTestUser("testuser", "test@example.com", "password123");
        String token = generateToken(user);
        String farFutureDate = "2030-12-31"; // Datum långt fram där det garanterat inte finns games

        // When & Then
        mockMvc.perform(get("/api/v1/games/" + farFutureDate)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNoContent()); // Enligt din API-dokumentation
    }
}