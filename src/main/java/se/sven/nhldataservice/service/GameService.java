package se.sven.nhldataservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import se.sven.nhldataservice.dto.*;
import se.sven.nhldataservice.model.*;
import se.sven.nhldataservice.repository.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final GamePersistenceService gamePersistenceService;

    private static final String BASE_URL = "https://api-web.nhle.com";
    private static final String API_ENDPOINT = "/v1/schedule/";

    /**
     * Retrieves NHL games for a given date with caching strategy.
     * Uses database cache for historical games, always fetches fresh data for today's games.
     *
     * @param date the date to retrieve games for
     * @return list of games for the specified date
     */
    public List<GameDTO> getGamesDtoWithFallback(LocalDate date) {
        if (shouldFetchFromApi(date)) {
            log.info("🔄 Fetching directly from API for {}", date);
            return fetchAndCacheGames(date);
        }

        return getCachedGamesOrFetchFromApi(date);
    }

    /**
     * Determines if data should be fetched directly from API instead of cache.
     * Today's games are always fetched fresh due to changing scores and status.
     */
    private boolean shouldFetchFromApi(LocalDate date) {
        return !date.isBefore(LocalDate.now());
    }

    /**
     * Attempts to retrieve games from database cache, falls back to API if not found.
     */
    private List<GameDTO> getCachedGamesOrFetchFromApi(LocalDate date) {
        String dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        List<Game> cachedGames = gameRepository.findAllByGameDate(dateString);

        if (!cachedGames.isEmpty()) {
            List<GameDTO> dtos = cachedGames.stream()
                    .map(this::mapGameToDTO)
                    .toList();
            log.info("📋 Returning {} games from database for {}", dtos.size(), date);
            return dtos;
        }

        return fetchAndCacheGames(date);
    }

    /**
     * Fetches games from API and caches them if they're not today's games.
     */
    private List<GameDTO> fetchAndCacheGames(LocalDate date) {
        List<GameDTO> dtos = fetchGamesFromApi(date);

        if (!dtos.isEmpty() && !shouldFetchFromApi(date)) {
            gamePersistenceService.saveGamesDtoToDB(dtos);
            log.info("💾 Saved {} games to database for {}", dtos.size(), date);
        }

        return dtos.stream()
                .filter(game -> {
                    try {
                        LocalDate gameDate = LocalDate.parse(game.getGameDate());
                        return gameDate.equals(date);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .toList();
    }

    /**
     * Fetches games from NHL API and returns as DTOs.
     */
    private List<GameDTO> fetchGamesFromApi(LocalDate date) {
        String url = buildApiUrl(date);
        log.info("🌐 Calling NHL API: {}", url);

        try {
            String jsonResponse = restTemplate.getForObject(url, String.class);
            return parseJsonToGameDTOs(jsonResponse);
        } catch (RestClientException e) {
            log.error("❌ Error during API call: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Builds the complete NHL API URL for the specified date.
     */
    private String buildApiUrl(LocalDate date) {
        String formattedDate = date.format(DateTimeFormatter.ISO_DATE);
        return BASE_URL + API_ENDPOINT + formattedDate;
    }

    /**
     * Parses NHL API JSON response into GameDTO objects.
     * Handles the nested gameWeek structure from the API.
     */
    private List<GameDTO> parseJsonToGameDTOs(String json) {
        try {
            logJsonPreview(json);

            ScheduleResponseDTO scheduleResponse = objectMapper.readValue(json, ScheduleResponseDTO.class);
            List<GameDTO> allGames = extractGamesFromSchedule(scheduleResponse);

            log.info("✅ Found {} games from API", allGames.size());
            return allGames;

        } catch (Exception e) {
            log.error("❌ JSON parsing failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private void logJsonPreview(String json) {
        log.debug("📄 Processing JSON response (first 200 characters): {}",
                json.length() > 200 ? json.substring(0, 200) + "..." : json);
    }

    /**
     * Extracts all games from the NHL API's nested gameWeek structure.
     */
    private List<GameDTO> extractGamesFromSchedule(ScheduleResponseDTO scheduleResponse) {
        List<GameDTO> allGames = new ArrayList<>();

        Optional.ofNullable(scheduleResponse.getGameWeek())
                .ifPresent(gameWeeks -> gameWeeks.forEach(week ->
                        addGamesFromWeek(week, allGames)));

        return allGames;
    }

    /**
     * Adds games from a single week to the master games list, setting the date for each game.
     */
    private void addGamesFromWeek(GameWeekDTO week, List<GameDTO> allGames) {
        Optional.ofNullable(week.getGames())
                .ifPresent(games -> games.forEach(game -> {
                    game.setGameDate(week.getDate());
                    allGames.add(game);
                }));
    }

    /**
     * Converts database Game entity to API response DTO format.
     */
    private GameDTO mapGameToDTO(Game game) {
        GameDTO dto = new GameDTO();

        setBasicGameData(dto, game);
        setVenueData(dto, game);
        setTeamData(dto, game);
        setPeriodData(dto, game);
        setGameOutcomeData(dto, game);
        setClockData(dto, game);

        return dto;
    }

    /**
     * Maps basic game information from entity to DTO.
     */
    private void setBasicGameData(GameDTO dto, Game game) {
        dto.setId(game.getId());
        dto.setSeason(game.getSeason());
        dto.setGameType(game.getGameType());
        dto.setGameDate(game.getGameDate());
        dto.setNeutralSite(game.getNeutralSite());
        dto.setStartTimeUTC(game.getStartTimeUTC());
        dto.setEasternUTCOffset(game.getEasternUTCOffset());
        dto.setVenueUTCOffset(game.getVenueUTCOffset());
        dto.setVenueTimezone(game.getVenueTimezone());
        dto.setGameState(game.getGameState());
        dto.setGameScheduleState(game.getGameScheduleState());
        dto.setGameCenterLink(game.getGameCenterLink());
    }

    /**
     * Maps venue information, creating LocalizedNameDTO structure expected by API consumers.
     */
    private void setVenueData(GameDTO dto, Game game) {
        dto.setVenue(createLocalizedNameDTO(game.getVenue()));
    }

    /**
     * Maps team data including current scores from the game.
     */
    private void setTeamData(GameDTO dto, Game game) {
        dto.setHomeTeam(mapTeamToDTO(game.getHomeTeam(), game.getHomeScore()));
        dto.setAwayTeam(mapTeamToDTO(game.getAwayTeam(), game.getAwayScore()));
    }

    /**
     * Maps period information if game has started.
     */
    private void setPeriodData(GameDTO dto, Game game) {
        if (game.getPeriod() > 0 || game.getPeriodType() != null) {
            PeriodDescriptorDTO periodDescriptor = new PeriodDescriptorDTO();
            periodDescriptor.setNumber(game.getPeriod());
            periodDescriptor.setPeriodType(game.getPeriodType());
            periodDescriptor.setMaxRegulationPeriods(game.getMaxRegulationPeriods());
            dto.setPeriodDescriptor(periodDescriptor);
        }
    }

    /**
     * Maps overtime/shootout outcome data if available.
     */
    private void setGameOutcomeData(GameDTO dto, Game game) {
        if (game.getOtPeriods() != null) {
            GameOutcomeDTO gameOutcome = new GameOutcomeDTO();
            gameOutcome.setOtPeriods(game.getOtPeriods());
            dto.setGameOutcome(gameOutcome);
        }
    }

    /**
     * Maps live game clock data if available (for ongoing games).
     */
    private void setClockData(GameDTO dto, Game game) {
        if (game.getTimeRemaining() != null || game.getSecondsRemaining() != null) {
            ClockDTO clock = new ClockDTO();
            clock.setTimeRemaining(game.getTimeRemaining());
            clock.setSecondsRemaining(game.getSecondsRemaining());
            clock.setRunning(game.getClockRunning());
            clock.setInIntermission(game.getInIntermission());
            dto.setClock(clock);
        }
    }

    /**
     * Converts Team entity to TeamDTO with score information.
     */
    private TeamDTO mapTeamToDTO(Team team, int score) {
        if (team == null) {
            return null;
        }

        TeamDTO dto = new TeamDTO();
        dto.setId(team.getId());
        dto.setAbbrev(team.getAbbrev());
        dto.setLogo(team.getLogo());
        dto.setScore(score);
        dto.setPlaceName(createLocalizedNameDTO(team.getCity()));
        dto.setName(createLocalizedNameDTO(team.getName()));

        return dto;
    }

    /**
     * Helper method to create LocalizedNameDTO from simple string value.
     * Returns null if input is null to avoid unnecessary object creation.
     */
    private LocalizedNameDTO createLocalizedNameDTO(String value) {
        if (value == null) {
            return null;
        }

        LocalizedNameDTO dto = new LocalizedNameDTO();
        dto.setDefaultValue(value);
        return dto;
    }
}