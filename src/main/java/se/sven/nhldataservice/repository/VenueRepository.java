package se.sven.nhldataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sven.nhldataservice.model.Venue;

public interface VenueRepository extends JpaRepository<Venue, Long> {
}
