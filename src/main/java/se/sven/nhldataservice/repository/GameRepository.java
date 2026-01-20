package se.sven.nhldataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sven.nhldataservice.model.Game;

import java.util.List;

/**
 * Repository for Game entities.
 * Provides database access for NHL game data with date-based queries.
 */
public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findAllByGameDate(String date);
}
