package se.sven.nhldataservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sven.nhldataservice.dto.TeamDTO;

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

    public Team(TeamDTO dto) {
        this.id = dto.getId();
        this.abbrev = dto.getAbbrev();
        // Fixa mappning från LocalizedNameDTO:
        this.name = dto.getName() != null ? dto.getName().getDefaultValue() : null;
        this.city = dto.getPlaceName() != null ? dto.getPlaceName().getDefaultValue() : null;
        this.logo = dto.getLogo();
    }
}