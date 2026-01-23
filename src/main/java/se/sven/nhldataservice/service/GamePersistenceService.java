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
     * Saves games to database with team caching to avoid duplicate lookups
     * and ensure proper entity relationships.
     *
     * @param dtos list of games to persist
     */
    public void saveGamesDtoToDB(List<GameDTO> dtos) {
        if (dtos.isEmpty()) {
            log.debug("No games to save");
            return;
        }

        log.info("Saving {} games to database", dtos.size());

        Map<Long, Team> teamCache = new HashMap<>();

        try {
            List<Game> gamesToSave = dtos.stream()
                    .map(dto -> createGameWithCachedTeams(dto, teamCache))
                    .toList();

            gameRepository.saveAll(gamesToSave);
            log.info("Successfully saved {} games", dtos.size());

        } catch (Exception e) {
            log.error("Failed to save games: {}", e.getMessage(), e);
            throw e;
        }
    }

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

    private Team getCachedOrSaveTeam(Team team, Map<Long, Team> teamCache) {
        return teamCache.computeIfAbsent(team.getId(), id ->
                teamRepository.findById(id)
                        .orElseGet(() -> saveNewTeam(team))
        );
    }

    private Team saveNewTeam(Team team) {
        Team savedTeam = teamRepository.save(team);
        log.debug("Saved new team: {} ({})", team.getName(), team.getId());
        return savedTeam;
    }
}