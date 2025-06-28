package se.sven.nhldataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sven.nhldataservice.model.Game;

import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findAllByGameDate(String date);
}
