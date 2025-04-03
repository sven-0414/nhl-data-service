package se.sven.nhldataservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * Data Transfer Object for transferring game data from the NHL API.
 * Contains metadata about the game, including time, state, and teams.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameDTO {
    private long id;
    private int season;
    private String gameCenterLink;
    private char gameType;
    private char gameState;
    private ZonedDateTime startTimeUTC;
    private TeamDTO homeTeam;
    private TeamDTO awayTeam;
    private VenueDTO venue;
}