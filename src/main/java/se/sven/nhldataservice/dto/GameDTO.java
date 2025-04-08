package se.sven.nhldataservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * Data Transfer Object for transferring basic game data from the NHL API.
 * Used for listing games on a specific date.
 *
 * @author [Sven Eriksson]
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameDTO {
    private long id;
    private int season;
    private int homeScore;
    private int awayScore;
    private int period;
    private char gameType;
    private char gameState;
    private String gameCenterLink;
    private ZonedDateTime startTimeUTC;
    private TeamDTO homeTeam;
    private TeamDTO awayTeam;
    private VenueDTO venue;
}