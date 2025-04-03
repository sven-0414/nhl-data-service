package se.sven.nhldataservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for transferring team data from the NHL API.
 * Represents either a home or away team in a game.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamDTO {
    private Long id;
    private String abbrev;
    private String name;
    private String city;
}