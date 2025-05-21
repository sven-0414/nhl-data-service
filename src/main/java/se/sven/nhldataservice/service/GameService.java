package se.sven.nhldataservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import se.sven.nhldataservice.dto.GameDTO;
import se.sven.nhldataservice.dto.ScheduleResponseDTO;
import se.sven.nhldataservice.dto.TeamDTO;
import se.sven.nhldataservice.dto.VenueDTO;
import se.sven.nhldataservice.model.Game;
import se.sven.nhldataservice.model.Team;
import se.sven.nhldataservice.model.Venue;
import se.sven.nhldataservice.repository.GameRepository;
import se.sven.nhldataservice.repository.TeamRepository;
import se.sven.nhldataservice.repository.VenueRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Serviceklass som ansvarar för att hämta NHL-matchdata från ett externt API.
 * Använder WebClient för att göra asynkrona HTTP-anrop till NHL:s schema-API.
 */
@Service
public class GameService {

    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;
    private final VenueRepository venueRepository;
    private final WebClient webClient;

    private static final String BASE_URL = "https://api-web.nhle.com/v1/schedule";

    /**
     * Konstruktor som bygger en WebClient instans med grund-URL för NHL:s schema-API.
     *
     * @param webClientBuilder WebClient. Builder som injiceras av Spring.
     */
    public GameService(WebClient.Builder webClientBuilder,
                       GameRepository gameRepository,
                       TeamRepository teamRepository,
                       VenueRepository venueRepository) {
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
        this.gameRepository = gameRepository;
        this.teamRepository = teamRepository;
        this.venueRepository = venueRepository;
    }

    /**
     * Hämtar matchdata för ett specifikt datum och konverterar svaret till en array av GameDTO.
     *
     * @param date Datum att hämta matcher för.
     * @return Ett Mono som innehåller en array av GameDTO.
     */
    public Mono<List<GameDTO>> fetchGamesAsDto(LocalDate date) {
        String formattedDate = date.format(DateTimeFormatter.ISO_DATE);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("") // 🔹 Behåll baseUrl, lägg bara till query params
                        .queryParam("startDate", formattedDate)
                        .queryParam("endDate", formattedDate)
                        .build())
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        response -> Mono.error(new RuntimeException("Inga matcher hittades.")))
                .bodyToMono(ScheduleResponseDTO.class)
                .doOnNext(dto -> {
                    System.out.println("⬇️ NHL ScheduleResponseDTO:");
                    System.out.println(dto);
                    if (dto == null) {
                        System.out.println("⚠️ dto är null!");
                    } else if (dto.getGames() == null) {
                        System.out.println("⚠️ dto.getGames() är null!");
                    } else if (dto.getGames().isEmpty()) {
                        System.out.println("⚠️ dto.getGames() är tom!");
                    } else {
                        System.out.println("✅ Antal matcher: " + dto.getGames().size());
                    }
                })
                .timeout(Duration.ofSeconds(5))
                .map(ScheduleResponseDTO::getGames)
                .onErrorResume(error -> Mono.just(Collections.emptyList()));
    }

    /**
     * Hämtar matcher för ett datum, mappar till entiteter och sparar i databasen.
     *
     * @param date Datum att importera matcher för
     */
    public Mono<List<Game>> getGamesWithFallback(LocalDate date) {
        return Mono.fromCallable(() -> gameRepository.findAllByNhlGameDate(date))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(gamesInDb -> {
                    if (!gamesInDb.isEmpty()) {
                        return Mono.just(gamesInDb);
                    }

                    // Hämta från NHL:s API – OBS! detta är redan ett Mono<List<GameDTO>>
                    return fetchGamesAsDto(date)
                            .flatMap(dtos -> Mono.fromCallable(() -> {
                                List<Game> saved = new ArrayList<>();
                                for (GameDTO dto : dtos) {
                                    ZonedDateTime startTime = dto.getStartTimeUTC(); // extrahera från DTO
                                    Game game = new Game(dto, startTime);
                                    teamRepository.save(game.getAwayTeam());
                                    venueRepository.save(game.getVenue());
                                    saved.add(gameRepository.save(game));
                                }
                                return saved;
                            }).subscribeOn(Schedulers.boundedElastic()));
                });
    }

    public Mono<List<GameDTO>> getGamesDtoWithFallback(LocalDate localDate) {
        return getGamesWithFallback(localDate)
                .map(games -> games.stream()
                        .map(this::mapToDTO)
                        .toList());
    }

    private GameDTO mapToDTO(Game game) {
        return new GameDTO(
                game.getId(),
                game.getSeason(),
                game.getHomeScore(),
                game.getAwayScore(),
                game.getPeriod(),
                game.getGameType(),
                game.getGameState(),
                game.getGameCenterLink(),
                game.getStartTimeUTC(),
                mapTeamToDTO(game.getHomeTeam()),
                mapTeamToDTO(game.getAwayTeam()),
                mapVenueToDTO(game.getVenue())
        );
    }

    private TeamDTO mapTeamToDTO(Team team) {
        TeamDTO dto = new TeamDTO();
        dto.setId(team.getId());
        dto.setAbbrev(team.getAbbrev()); // modellen heter "abbreviation", DTO "abbrev"
        dto.setName(team.getName());
        dto.setCity(team.getCity());
        return dto;
    }

    private VenueDTO mapVenueToDTO(Venue venue) {
        VenueDTO dto = new VenueDTO();
        dto.setId(venue.getId());
        dto.setName(venue.getName());
        dto.setCity(venue.getCity());
        dto.setState(venue.getState());
        dto.setCountry(venue.getCountry());
        dto.setVenueTimezone(venue.getVenueTimezone());
        return dto;
    }
}