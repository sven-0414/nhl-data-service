package se.sven.nhldataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sven.nhldataservice.model.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
