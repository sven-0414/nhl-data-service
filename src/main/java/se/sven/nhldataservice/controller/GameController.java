package se.sven.nhldataservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<List<GameDTO>> getGames(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<GameDTO> games = gameService.getGamesDtoWithFallback(date);

        if (games.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(games);
    }
}