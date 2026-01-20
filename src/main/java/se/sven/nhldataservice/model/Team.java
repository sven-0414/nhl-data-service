package se.sven.nhldataservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sven.nhldataservice.dto.TeamDTO;

/**
 * Entity representing an NHL team.
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
    private String logo;

    /**
     * Maps from NHL API's TeamDTO, extracting localized names from nested objects.
     * @param dto NHL API team data
     */
    public Team(TeamDTO dto) {
        this.id = dto.getId();
        this.abbrev = dto.getAbbrev();
        this.name = dto.getName() != null ? dto.getName().getDefaultValue() : null;
        this.city = dto.getPlaceName() != null ? dto.getPlaceName().getDefaultValue() : null;
        this.logo = dto.getLogo();
    }
}