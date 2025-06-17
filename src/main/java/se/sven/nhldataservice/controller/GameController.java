package se.sven.nhldataservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping("/{date}")
    public ResponseEntity<List<GameDTO>> getGames(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("🎯 Hämtar matcher för datum: {}", date);

        List<GameDTO> games = gameService.getGamesDtoWithFallback(date);

        if (games.isEmpty()) {
            log.info("📭 Inga matcher hittades för {}", date);
            return ResponseEntity.noContent().build();
        }

        log.info("📤 Returnerar {} matcher för {}", games.size(), date);
        return ResponseEntity.ok(games);
    }
}