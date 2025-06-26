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
     * Hämtar matcher för ett datum - returnerar DTO:er direkt från API eller konverterar från databas
     */
    public List<GameDTO> getGamesDtoWithFallback(LocalDate date) {
        if (shouldFetchFromApi(date)) {
            log.info("🔄 Hämtar direkt från API för {}", date);
            return fetchAndCacheGames(date);
        }

        return getCachedGamesOrFetchFromApi(date);
    }

    private boolean shouldFetchFromApi(LocalDate date) {
        return date.equals(LocalDate.now()); // Dagens matcher hämtas alltid från API
    }

    private List<GameDTO> getCachedGamesOrFetchFromApi(LocalDate date) {
        List<Game> cachedGames = gameRepository.findAllByNhlGameDate(date);

        if (!cachedGames.isEmpty()) {
            List<GameDTO> dtos = cachedGames.stream()
                    .map(this::mapGameToDTO)
                    .toList();
            log.info("📋 Returnerar {} matcher från databas för {}", dtos.size(), date);
            return dtos;
        }

        return fetchAndCacheGames(date);
    }

    private List<GameDTO> fetchAndCacheGames(LocalDate date) {
        List<GameDTO> dtos = fetchGamesFromApi(date);

        if (!dtos.isEmpty() && !shouldFetchFromApi(date)) {
            gamePersistenceService.saveGamesDtoToDB(dtos);
            log.info("💾 Sparade {} matcher i databas för {}", dtos.size(), date);
        }

        return dtos;
    }

    /**
     * Hämtar matcher från NHL API och returnerar som DTO:er
     */
    private List<GameDTO> fetchGamesFromApi(LocalDate date) {
        String url = buildApiUrl(date);
        log.info("🌐 Anropar NHL API: {}", url);

        try {
            String jsonResponse = restTemplate.getForObject(url, String.class);
            return parseJsonToGameDTOs(jsonResponse);
        } catch (RestClientException e) {
            log.error("❌ Fel vid API-anrop: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private String buildApiUrl(LocalDate date) {
        String formattedDate = date.format(DateTimeFormatter.ISO_DATE);
        return BASE_URL + API_ENDPOINT + formattedDate;
    }

    /**
     * Parsar JSON-svar från NHL API till GameDTO-lista
     */
    private List<GameDTO> parseJsonToGameDTOs(String json) {
        try {
            logJsonPreview(json);

            ScheduleResponseDTO scheduleResponse = objectMapper.readValue(json, ScheduleResponseDTO.class);
            List<GameDTO> allGames = extractGamesFromSchedule(scheduleResponse);

            log.info("✅ Hittade {} matcher från API", allGames.size());
            return allGames;

        } catch (Exception e) {
            log.error("❌ JSON-parsning misslyckades: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private void logJsonPreview(String json) {
        log.debug("📄 Bearbetar JSON-svar (första 200 tecken): {}",
                json.length() > 200 ? json.substring(0, 200) + "..." : json);
    }

    private List<GameDTO> extractGamesFromSchedule(ScheduleResponseDTO scheduleResponse) {
        List<GameDTO> allGames = new ArrayList<>();

        Optional.ofNullable(scheduleResponse.getGameWeek())
                .ifPresent(gameWeeks -> gameWeeks.forEach(week ->
                        addGamesFromWeek(week, allGames)));

        return allGames;
    }

    private void addGamesFromWeek(GameWeekDTO week, List<GameDTO> allGames) {
        Optional.ofNullable(week.getGames())
                .ifPresent(games -> games.forEach(game -> {
                    game.setGameDate(week.getDate());
                    allGames.add(game);
                }));
    }

    /**
     * Konverterar Game-entitet till GameDTO för API-respons
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

    private void setVenueData(GameDTO dto, Game game) {
        dto.setVenue(createLocalizedNameDTO(game.getVenue()));
    }

    private void setTeamData(GameDTO dto, Game game) {
        dto.setHomeTeam(mapTeamToDTO(game.getHomeTeam(), game.getHomeScore()));
        dto.setAwayTeam(mapTeamToDTO(game.getAwayTeam(), game.getAwayScore()));
    }

    private void setPeriodData(GameDTO dto, Game game) {
        if (game.getPeriod() > 0 || game.getPeriodType() != null) {
            PeriodDescriptorDTO periodDescriptor = new PeriodDescriptorDTO();
            periodDescriptor.setNumber(game.getPeriod());
            periodDescriptor.setPeriodType(game.getPeriodType());
            periodDescriptor.setMaxRegulationPeriods(game.getMaxRegulationPeriods());
            dto.setPeriodDescriptor(periodDescriptor);
        }
    }

    private void setGameOutcomeData(GameDTO dto, Game game) {
        if (game.getOtPeriods() != null) {
            GameOutcomeDTO gameOutcome = new GameOutcomeDTO();
            gameOutcome.setOtPeriods(game.getOtPeriods());
            dto.setGameOutcome(gameOutcome);
        }
    }

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
     * Konverterar Team-entitet till TeamDTO
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
     * Hjälpmetod för att skapa LocalizedNameDTO
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