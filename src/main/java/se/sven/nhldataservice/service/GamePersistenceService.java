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
     * Sparar GameDTOs som Game-entiteter i databasen
     * Optimerad version som cachar teams för att minska databasanrop
     */
    public void saveGamesDtoToDB(List<GameDTO> dtos) {
        if (dtos.isEmpty()) {
            log.info("📭 Inga matcher att spara");
            return;
        }

        log.info("💾 Börjar spara {} matcher i databas", dtos.size());

        // Cachea teams för att undvika upprepade databasanrop
        Map<Long, Team> teamCache = new HashMap<>();

        List<Game> gamesToSave = dtos.stream()
                .map(dto -> createGameWithCachedTeams(dto, teamCache))
                .toList();

        gameRepository.saveAll(gamesToSave);
        log.info("💾 Slutförde sparning av {} matcher", dtos.size());
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

    /**
     * Hämtar team från cache eller sparar/hämtar från databas
     * Optimerad version som minskar antalet databasanrop
     */
    private Team getCachedOrSaveTeam(Team team, Map<Long, Team> teamCache) {
        return teamCache.computeIfAbsent(team.getId(), id ->
                teamRepository.findById(id)
                        .orElseGet(() -> saveNewTeam(team))
        );
    }

    private Team saveNewTeam(Team team) {
        Team savedTeam = teamRepository.save(team);
        log.debug("🏒 Sparade nytt team: {} ({})", team.getName(), team.getId());
        return savedTeam;
    }
}