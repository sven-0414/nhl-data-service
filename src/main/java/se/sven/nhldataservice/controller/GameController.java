package se.sven.nhldataservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * REST controller for NHL games with database caching and API fallback.
 */
@Tag(name = "Games", description = "NHL game data operations")
@Slf4j
@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @Operation(
            summary = "Get NHL games by date",
            description = "Retrieves all NHL games for a specific date with database caching and API fallback"
    )
    @ApiResponse(responseCode = "200", description = "Games found")
    @ApiResponse(responseCode = "204", description = "No games found for the specified date")
    @ApiResponse(responseCode = "400", description = "Invalid date format")    @GetMapping("/{date}")
    public ResponseEntity<List<GameDTO>> getGames(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("🎯 Fetching games for date: {}", date);

        List<GameDTO> games = gameService.getGamesDtoWithFallback(date);

        return buildResponse(games, date);
    }

    private ResponseEntity<List<GameDTO>> buildResponse(List<GameDTO> games, LocalDate date) {
        if (games.isEmpty()) {
            log.info("📭 No games found for {}", date);
            return ResponseEntity.noContent().build();
        }

        log.info("📤 Returning {} games for {}", games.size(), date);
        return ResponseEntity.ok(games);
    }
}