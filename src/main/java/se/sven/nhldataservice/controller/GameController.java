package se.sven.nhldataservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import se.sven.nhldataservice.dto.GameDTO;
import se.sven.nhldataservice.service.GameService;

import java.time.LocalDate;
import java.util.List;

/**
 * REST-kontroller för att hämta NHL-matcher.
 * Hämtar från databasen om data finns, annars från NHL:s API och returnerar en JSON.
 */
@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    /**
     * Hämtar matcher för ett visst datum.
     * Om matcherna inte finns i databasen hämtas de från NHL:s API och sparas.
     *
     * @param date Datum i format YYYY-MM-DD
     * @return Lista med matcher i JSON-format
     */
    @GetMapping("/{date}")
    public Mono<List<GameDTO>> getGames(@PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);
        return gameService.getGamesDtoWithFallback(localDate);
    }
}
