package se.sven.nhldataservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}