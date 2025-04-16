package se.sven.nhldataservice.service;

import org.springframework.beans.factory.annotation.Autowired;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Serviceklass som ansvarar för att hämta NHL-matchdata från ett externt API.
 * Använder WebClient för att göra asynkrona HTTP-anrop till NHL:s schema-API.
 */
@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private VenueRepository venueRepository;

    private final WebClient webClient;
    private static final String BASE_URL = "https://api-web.nhle.com/v1/schedule";

/**
 * Konstruktor som bygger en WebClient instans med grund-URL för NHL:s schema-API.
 *
 * @param webClientBuilder WebClient. Builder som injiceras av Spring.
 */
    public GameService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
    }

    /**
     * Hämtar matchdata för ett specifikt datum från NHL:s schema-API.
     * Datumet formatteras enligt ISO 8601 (yyyy-MM-dd).
     *
     * @param date Datum att hämta matcher för.
     * @return Ett Mono som innehåller svaret från API:et som en JSON-sträng.
     */
    public Mono<String> fetchGamesForDate(LocalDate date) {
        String formattedDate = date.format(DateTimeFormatter.ISO_DATE);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("startDate", formattedDate)
                        .queryParam("endDate", formattedDate)
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * Hämtar matchdata för dagens datum.
     *
     * @return Ett Mono som innehåller dagens matcher i form av en JSON-sträng.
     */
    public Mono<String> fetchGamesToday() {
        return fetchGamesForDate(LocalDate.now());
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
    public void importAndSaveGames(LocalDate date) {
        fetchGamesAsDto(date).subscribe(gameDTOs -> {
            for (GameDTO dto : gameDTOs) {
                Game game = new Game(dto);

                teamRepository.save(game.getHomeTeam());
                teamRepository.save(game.getAwayTeam());
                venueRepository.save(game.getVenue());
                gameRepository.save(game);
            }
        });
    }
}