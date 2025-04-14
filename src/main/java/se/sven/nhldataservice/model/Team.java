package se.sven.nhldataservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sven.nhldataservice.dto.TeamDTO;

/**
 * Represents a hockey team with its ID, abbreviation, name, and city.
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Team {
    @Id
    private Long id;
    private String abbrev;
    private String name;
    private String city;

    /**
     * Skapar en ny Team-entitet baserat på en DTO från NHL:s API.
     *
     * @param dto Data Transfer Object som innehåller laginformation.
     */
    public Team(TeamDTO dto) {
        this.id = dto.getId();
        this.abbrev = dto.getAbbrev();
        this.name = dto.getName();
        this.city = dto.getCity();
    }
}