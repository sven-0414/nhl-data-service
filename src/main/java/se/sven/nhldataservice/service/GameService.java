package se.sven.nhldataservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import se.sven.nhldataservice.dto.GameDTO;
import se.sven.nhldataservice.dto.ScheduleResponseDTO;
import se.sven.nhldataservice.model.Game;
import se.sven.nhldataservice.repository.GameRepository;
import se.sven.nhldataservice.repository.TeamRepository;
import se.sven.nhldataservice.repository.VenueRepository;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
                        .queryParam("startDate", formattedDate)
                        .queryParam("endDate", formattedDate)
                        .build())
                .retrieve()
                .bodyToMono(ScheduleResponseDTO.class)
                .map(ScheduleResponseDTO::getGames);
    }

    /**
     * Hämtar matcher för ett datum, mappar till entiteter och sparar i databasen.
     *
     * @param date Datum att importera matcher för
     */
    public Mono<List<Game>> getGamesWithFallback(LocalDate date) {
        List<Game> gamesInDb = gameRepository.findAllByNhlGameDate(date);

        if (!gamesInDb.isEmpty()) {
            return Mono.just(gamesInDb);
        }

        return fetchGamesAsDto(date)
                .map(dtos -> {
                    List<Game> saved = new ArrayList<>();
                    for (GameDTO dto : dtos) {
                        Game game = new Game(dto, date); // 🔹 spara med NHL-datumet
                        teamRepository.save(game.getHomeTeam());
                        teamRepository.save(game.getAwayTeam());
                        venueRepository.save(game.getVenue());
                        saved.add(gameRepository.save(game));
                    }
                    return saved;
                });
    }
}