package se.sven.nhldataservice.controller;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import se.sven.nhldataservice.model.Game;
import se.sven.nhldataservice.service.GameService;

import java.time.LocalDate;
import java.util.List;

/**
 * REST-kontroller för att hämta NHL-matcher.
 * Hämtar från databasen om data finns, annars från NHL:s API och sparar.
 */
@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Hämtar matcher för ett visst datum.
     * Om matcherna inte finns i databasen hämtas de från NHL:s API och sparas.
     *
     * @param date Datum i format YYYY-MM-DD
     * @return Lista med matcher i JSON-format
     */
    @GetMapping("/{date}")
    public Mono<List<Game>> getGames(@PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);
        return gameService.getGamesWithFallback(localDate);
    }
}
