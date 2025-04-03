package se.sven.nhldataservice.model;

import jakarta.persistence.*;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a hockey game with details such as time, teams, venue, and status.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class Game {
    @Id
    private long id;
    private int season;
    private String gameCenterLink;
    private char gameType;
    private char gameState;
    private ZonedDateTime startTimeUTC;
    @ManyToOne
    private Team homeTeam;
    @ManyToOne
    private Team awayTeam;
    @ManyToOne
    private Venue venue;
}