package se.sven.nhldataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sven.nhldataservice.model.Team;

/**
 * Repository for Team entities.
 * Provides standard CRUD operations for NHL teams.
 */
public interface TeamRepository extends JpaRepository<Team, Long> {
}
