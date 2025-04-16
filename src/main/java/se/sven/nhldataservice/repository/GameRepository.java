package se.sven.nhldataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sven.nhldataservice.model.Game;

public interface GameRepository extends JpaRepository<Game, Long> {
}
