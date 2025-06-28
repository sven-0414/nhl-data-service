package se.sven.nhldataservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import se.sven.nhldataservice.dto.GameDTO;
import se.sven.nhldataservice.dto.GameWeekDTO;
import se.sven.nhldataservice.dto.ScheduleResponseDTO;
import se.sven.nhldataservice.model.Game;
import se.sven.nhldataservice.model.Team;
import se.sven.nhldataservice.repository.GameRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private GamePersistenceService gamePersistenceService;

    @InjectMocks
    private GameService gameService;

    @Test
    void shouldFetchFromApiForTodaysDate() throws Exception {
        // Given
        LocalDate today = LocalDate.now();
        String expectedUrl = "https://api-web.nhle.com/v1/schedule/" + today;
        String mockJsonResponse = "{\"gameWeek\":[]}"; // Minimal NHL API response

        when(restTemplate.getForObject(expectedUrl, String.class))
                .thenReturn(mockJsonResponse);
        when(objectMapper.readValue(mockJsonResponse, ScheduleResponseDTO.class))
                .thenReturn(new ScheduleResponseDTO()); // Tom response

        // When
        List<GameDTO> result = gameService.getGamesDtoWithFallback(today);

        // Then
        verify(restTemplate).getForObject(expectedUrl, String.class);
        verify(gameRepository, never()).findAllByGameDate(any()); // Ska INTE kolla databas
        assertThat(result).isEmpty();
    }

    @Test
    void shouldCheckDatabaseForHistoricalDate() throws Exception {
        // Given
        LocalDate historicalDate = LocalDate.now().minusDays(7);
        String expectedDateString = historicalDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

        when(gameRepository.findAllByGameDate(expectedDateString))
                .thenReturn(Collections.emptyList());

        String expectedUrl = "https://api-web.nhle.com/v1/schedule/" + historicalDate;
        String mockJsonResponse = """
        {
            "gameWeek": [
                {
                    "date": "2025-06-20",
                    "games": [
                        {
                            "id": 123,
                            "season": 20242025,
                            "gameDate": "2025-06-20"
                        }
                    ]
                }
            ]
        }
        """;

        // Mock response med DATA istället för tom lista
        ScheduleResponseDTO mockScheduleResponse = new ScheduleResponseDTO();
        GameWeekDTO mockWeek = new GameWeekDTO();
        mockWeek.setDate(expectedDateString);
        GameDTO mockGame = new GameDTO();
        mockGame.setId(123);
        mockWeek.setGames(List.of(mockGame)); // VIKTIGT: Inte tom lista!
        mockScheduleResponse.setGameWeek(List.of(mockWeek));

        when(restTemplate.getForObject(expectedUrl, String.class))
                .thenReturn(mockJsonResponse);
        when(objectMapper.readValue(mockJsonResponse, ScheduleResponseDTO.class))
                .thenReturn(mockScheduleResponse);

        // When
        List<GameDTO> result = gameService.getGamesDtoWithFallback(historicalDate);

        // Then
        verify(gameRepository).findAllByGameDate(expectedDateString);
        verify(restTemplate).getForObject(expectedUrl, String.class);
        verify(gamePersistenceService).saveGamesDtoToDB(any()); // Nu kommer det sparas!
        assertThat(result).hasSize(1);
    }

    @Test
    void shouldFetchFromApiForFutureDate() throws Exception {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(3);
        String expectedUrl = "https://api-web.nhle.com/v1/schedule/" + futureDate;
        String mockJsonResponse = """
            {
                "gameWeek": [
                    {
                        "date": "2025-06-30",
                        "games": []
                    }
                ]
            }
            """;

        ScheduleResponseDTO mockScheduleResponse = new ScheduleResponseDTO();
        mockScheduleResponse.setGameWeek(Collections.emptyList());

        when(restTemplate.getForObject(expectedUrl, String.class))
                .thenReturn(mockJsonResponse);
        when(objectMapper.readValue(mockJsonResponse, ScheduleResponseDTO.class))
                .thenReturn(mockScheduleResponse);

        // When
        List<GameDTO> result = gameService.getGamesDtoWithFallback(futureDate);

        // Then
        verify(restTemplate).getForObject(expectedUrl, String.class);
        verify(gameRepository, never()).findAllByGameDate(any(String.class));
        verify(gamePersistenceService, never()).saveGamesDtoToDB(any()); // Ska INTE spara framtida data
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnCachedGamesWhenFoundInDatabase() {
        // Given
        LocalDate historicalDate = LocalDate.now().minusDays(5);
        String expectedDateString = historicalDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

        // Mock Game entity från databas
        Game mockGame = new Game();
        mockGame.setId(12345L);
        mockGame.setSeason(20242025);
        mockGame.setGameDate("2025-06-22");
        mockGame.setGameState("FINAL");
        mockGame.setHomeScore(3);
        mockGame.setAwayScore(2);

        // Mock teams
        Team homeTeam = new Team();
        homeTeam.setId(1L);
        homeTeam.setName("Boston Bruins");
        homeTeam.setCity("Boston");
        homeTeam.setAbbrev("BOS");

        Team awayTeam = new Team();
        awayTeam.setId(2L);
        awayTeam.setName("Montreal Canadiens");
        awayTeam.setCity("Montreal");
        awayTeam.setAbbrev("MTL");

        mockGame.setHomeTeam(homeTeam);
        mockGame.setAwayTeam(awayTeam);

        when(gameRepository.findAllByGameDate(expectedDateString))
                .thenReturn(List.of(mockGame));

        // When
        List<GameDTO> result = gameService.getGamesDtoWithFallback(historicalDate);

        // Then
        verify(gameRepository).findAllByGameDate(expectedDateString);
        verify(restTemplate, never()).getForObject(any(String.class), eq(String.class));
        verify(gamePersistenceService, never()).saveGamesDtoToDB(any());

        assertThat(result).hasSize(1);
        GameDTO gameDto = result.getFirst();
        assertThat(gameDto.getId()).isEqualTo(12345L);
        assertThat(gameDto.getGameState()).isEqualTo("FINAL");
        assertThat(gameDto.getHomeTeam().getScore()).isEqualTo(3);
        assertThat(gameDto.getAwayTeam().getScore()).isEqualTo(2);
        assertThat(gameDto.getHomeTeam().getName().getDefaultValue()).isEqualTo("Boston Bruins");
    }

    @Test
    void shouldReturnEmptyListWhenApiCallFails() throws Exception {
        // Given
        LocalDate today = LocalDate.now();
        String expectedUrl = "https://api-web.nhle.com/v1/schedule/" + today;

        when(restTemplate.getForObject(expectedUrl, String.class))
                .thenThrow(new RestClientException("API connection failed"));

        // When
        List<GameDTO> result = gameService.getGamesDtoWithFallback(today);

        // Then
        verify(restTemplate).getForObject(expectedUrl, String.class);
        verify(objectMapper, never()).readValue(any(String.class), eq(ScheduleResponseDTO.class));
        verify(gamePersistenceService, never()).saveGamesDtoToDB(any());

        assertThat(result).isEmpty();
    }
}