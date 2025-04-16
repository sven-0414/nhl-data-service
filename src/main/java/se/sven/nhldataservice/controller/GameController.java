package se.sven.nhldataservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.sven.nhldataservice.service.GameService;

import java.time.LocalDate;

/**
 * REST-kontroller för att hantera NHL-matcher.
 * Här kan man trigga import av matcher från NHL:s API.
 */
@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Importerar och sparar matcher för ett specifikt datum från NHL:s API.
     *
     * @param date Datum i formatet YYYY-MM-DD
     * @return OK-svar när importen påbörjats
     */
    @PostMapping("/import/{date}")
    public ResponseEntity<String> importGames(@PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date); // Enkelt format, t.ex. 2025-04-14
        gameService.importAndSaveGames(localDate);
        return ResponseEntity.ok("Import påbörjad för " + date);
    }
}
