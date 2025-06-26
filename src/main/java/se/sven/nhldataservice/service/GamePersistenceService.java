package se.sven.nhldataservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.sven.nhldataservice.dto.GameDTO;
import se.sven.nhldataservice.model.Game;
import se.sven.nhldataservice.model.Team;
import se.sven.nhldataservice.repository.GameRepository;
import se.sven.nhldataservice.repository.TeamRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GamePersistenceService {

    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;

    /**
     * Saves NHL game data to the database with optimized team handling to avoid duplicate saves.
     *
     * @param dtos list of games to persist
     */
    public void saveGamesDtoToDB(List<GameDTO> dtos) {
        if (dtos.isEmpty()) {
            log.info("📭 No games to save");
            return;
        }

        log.info("💾 Starting to save {} games to database", dtos.size());

        Map<Long, Team> teamCache = new HashMap<>();

        List<Game> gamesToSave = dtos.stream()
                .map(dto -> createGameWithCachedTeams(dto, teamCache))
                .toList();

        gameRepository.saveAll(gamesToSave);
        log.info("💾 Completed saving {} games", dtos.size());
    }

    /**
     * Creates a Game entity from DTO with cached team references to avoid duplicate database calls.
     */
    private Game createGameWithCachedTeams(GameDTO dto, Map<Long, Team> teamCache) {
        Game game = new Game(dto);

        if (game.getHomeTeam() != null) {
            Team homeTeam = getCachedOrSaveTeam(game.getHomeTeam(), teamCache);
            game.setHomeTeam(homeTeam);
        }

        if (game.getAwayTeam() != null) {
            Team awayTeam = getCachedOrSaveTeam(game.getAwayTeam(), teamCache);
            game.setAwayTeam(awayTeam);
        }

        return game;
    }

    /**
     * Retrieves team from cache or fetches/saves from the database if not found.
     * Optimized to reduce database round trips during batch game saves.
     */
    private Team getCachedOrSaveTeam(Team team, Map<Long, Team> teamCache) {
        return teamCache.computeIfAbsent(team.getId(), id ->
                teamRepository.findById(id)
                        .orElseGet(() -> saveNewTeam(team))
        );
    }

    /**
     * Persists a new team to database and logs the operation.
     */
    private Team saveNewTeam(Team team) {
        Team savedTeam = teamRepository.save(team);
        log.debug("🏒 Saved new team: {} ({})", team.getName(), team.getId());
        return savedTeam;
    }
}