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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GamePersistenceService {

    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;

    /**
     * Sparar GameDTOs som Game-entiteter i databasen
     */
    public void saveGamesDtoToDB(List<GameDTO> dtos) {
        log.info("💾 Börjar spara {} matcher i databas", dtos.size());

        for (GameDTO dto : dtos) {
            Game game = new Game(dto);

            // Hantera teams separat för att undvika duplicering
            if (game.getHomeTeam() != null) {
                Team homeTeam = saveOrGetTeam(game.getHomeTeam());
                game.setHomeTeam(homeTeam);
            }
            if (game.getAwayTeam() != null) {
                Team awayTeam = saveOrGetTeam(game.getAwayTeam());
                game.setAwayTeam(awayTeam);
            }

            gameRepository.save(game);
            log.debug("✅ Sparade match ID: {}", game.getId());
        }

        log.info("💾 Slutförde sparning av {} matcher", dtos.size());
    }

    /**
     * Sparar eller hämtar existerande team från databasen
     */
    private Team saveOrGetTeam(Team team) {
        return teamRepository.findById(team.getId())
                .orElseGet(() -> {
                    Team savedTeam = teamRepository.save(team);
                    log.debug("🏒 Sparade nytt team: {} ({})", team.getName(), team.getId());
                    return savedTeam;
                });
    }
}