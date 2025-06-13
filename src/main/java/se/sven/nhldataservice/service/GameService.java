package se.sven.nhldataservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String BASE_URL = "https://api-web.nhle.com";

    /**
     * Hämtar matcher för ett datum - returnerar DTO:er direkt från API eller konverterar från databas
     */
    @Transactional
    public List<GameDTO> getGamesDtoWithFallback(LocalDate date) {
        // Kolla först i databasen
        List<Game> gamesInDb = gameRepository.findAllByNhlGameDate(date);

        if (!gamesInDb.isEmpty()) {
            // Konvertera från databas till DTO
            List<GameDTO> dtos = gamesInDb.stream()
                    .map(this::mapGameToDTO)
                    .toList();
            log.info("📋 Returnerar {} matcher från databas för {}", dtos.size(), date);
            return dtos;
        }

        // Hämta från API och spara i databas
        List<GameDTO> dtos = fetchGamesFromApi(date);
        if (!dtos.isEmpty()) {
            saveGamesDtoToDB(dtos);
            log.info("💾 Sparade {} matcher i databas för {}", dtos.size(), date);
        }

        return dtos;
    }

    /**
     * Hämtar matcher från NHL API och returnerar som DTO:er
     */
    private List<GameDTO> fetchGamesFromApi(LocalDate date) {
        String formattedDate = date.format(DateTimeFormatter.ISO_DATE);
        String url = BASE_URL + "/v1/schedule/" + formattedDate;

        log.info("🌐 Anropar NHL API: {}", url);

        try {
            String jsonResponse = restTemplate.getForObject(url, String.class);
            return parseJsonToGameDTOs(jsonResponse);
        } catch (RestClientException e) {
            log.error("❌ Fel vid API-anrop: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Parsar JSON-svar från NHL API till GameDTO-lista
     */
    private List<GameDTO> parseJsonToGameDTOs(String json) {
        try {
            log.debug("📄 Bearbetar JSON-svar (första 200 tecken): {}",
                    json.length() > 200 ? json.substring(0, 200) + "..." : json);

            ScheduleResponseDTO scheduleResponse = objectMapper.readValue(json, ScheduleResponseDTO.class);

            List<GameDTO> allGames = new ArrayList<>();
            if (scheduleResponse.getGameWeek() != null) {
                for (GameWeekDTO week : scheduleResponse.getGameWeek()) {
                    if (week.getGames() != null) {
                        allGames.addAll(week.getGames());
                    }
                }
            }

            log.info("✅ Hittade {} matcher från API", allGames.size());
            return allGames;

        } catch (Exception e) {
            log.error("❌ JSON-parsning misslyckades: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Sparar GameDTOs som Game-entiteter i databasen
     */
    public void saveGamesDtoToDB(List<GameDTO> dtos) {
        for (GameDTO dto : dtos) {
            Game game = new Game(dto);

            // Hantera teams separat för att undvika duplicering
            if (game.getHomeTeam() != null) {
                Team homeTeam = saveOrGetTeam(game.getHomeTeam());
                game.setHomeTeam(homeTeam);
            }
            if (game.getAwayTeam() != null) {
                Team awayTeam = saveOrGetTeam(game.getAwayTeam());
                game.setAwayTeam(awayTeam);
            }

            gameRepository.save(game);
        }
    }

    /**
     * Sparar eller hämtar existerande team från databasen
     */
    private Team saveOrGetTeam(Team team) {
        return teamRepository.findById(team.getId())
                .orElseGet(() -> teamRepository.save(team));
    }

    /**
     * Konverterar Game-entitet till GameDTO för API-respons
     */
    private GameDTO mapGameToDTO(Game game) {
        GameDTO dto = new GameDTO();

        // Grundläggande matchdata
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

        // Venue som LocalizedNameDTO
        if (game.getVenue() != null) {
            LocalizedNameDTO venue = new LocalizedNameDTO();
            venue.setDefaultValue(game.getVenue());
            dto.setVenue(venue);
        }

        // Teams med score
        dto.setHomeTeam(mapTeamToDTO(game.getHomeTeam(), game.getHomeScore()));
        dto.setAwayTeam(mapTeamToDTO(game.getAwayTeam(), game.getAwayScore()));

        // PeriodDescriptor
        if (game.getPeriod() > 0 || game.getPeriodType() != null) {
            PeriodDescriptorDTO periodDescriptor = new PeriodDescriptorDTO();
            periodDescriptor.setNumber(game.getPeriod());
            periodDescriptor.setPeriodType(game.getPeriodType());
            periodDescriptor.setMaxRegulationPeriods(game.getMaxRegulationPeriods());
            dto.setPeriodDescriptor(periodDescriptor);
        }

        // GameOutcome
        if (game.getLastPeriodType() != null || game.getOtPeriods() != null) {
            GameOutcomeDTO gameOutcome = new GameOutcomeDTO();
            gameOutcome.setLastPeriodType(game.getLastPeriodType());
            gameOutcome.setOtPeriods(game.getOtPeriods());
            dto.setGameOutcome(gameOutcome);
        }

        // Clock (för live-matcher)
        if (game.getTimeRemaining() != null || game.getSecondsRemaining() != null) {
            ClockDTO clock = new ClockDTO();
            clock.setTimeRemaining(game.getTimeRemaining());
            clock.setSecondsRemaining(game.getSecondsRemaining());
            clock.setRunning(game.getClockRunning());
            clock.setInIntermission(game.getInIntermission());
            dto.setClock(clock);
        }

        // WinnerByPeriod
        if (game.getWinnerByPeriodList() != null || game.getWinnerByPeriodGameOutcome() != null) {
            WinnerDTO winnerByPeriod = new WinnerDTO();
            winnerByPeriod.setPeriods(game.getWinnerByPeriodList());
            winnerByPeriod.setGameOutcome(game.getWinnerByPeriodGameOutcome());
            dto.setWinnerByPeriod(winnerByPeriod);
        }

        // WinnerByGameOutcome
        if (game.getWinnerByGameOutcomePeriods() != null || game.getWinnerByGameOutcomeResult() != null) {
            WinnerDTO winnerByGameOutcome = new WinnerDTO();
            winnerByGameOutcome.setPeriods(game.getWinnerByGameOutcomePeriods());
            winnerByGameOutcome.setGameOutcome(game.getWinnerByGameOutcomeResult());
            dto.setWinnerByGameOutcome(winnerByGameOutcome);
        }

        return dto;
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

        // Skapa LocalizedNameDTO för city
        if (team.getCity() != null) {
            LocalizedNameDTO placeName = new LocalizedNameDTO();
            placeName.setDefaultValue(team.getCity());
            dto.setPlaceName(placeName);
        }

        // Skapa LocalizedNameDTO för name
        if (team.getName() != null) {
            LocalizedNameDTO name = new LocalizedNameDTO();
            name.setDefaultValue(team.getName());
            dto.setName(name);
        }

        return dto;
    }
}