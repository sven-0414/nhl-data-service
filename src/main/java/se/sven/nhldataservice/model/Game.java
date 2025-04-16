package se.sven.nhldataservice.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sven.nhldataservice.dto.GameDTO;

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
    private LocalDate nhlGameDate;
    @ManyToOne
    private Team homeTeam;
    @ManyToOne
    private Team awayTeam;
    @ManyToOne
    private Venue venue;

    /**
     * Skapar en ny Game-entitet baserat på en DTO från NHL:s API.
     * Konverterar även underliggande TeamDTO och VenueDTO till entiteter.
     *
     * @param dto Data Transfer Object som innehåller matchinformation.
     */
    public Game(GameDTO dto, LocalDate nhlDate) {
        this.id = dto.getId();
        this.season = dto.getSeason();
        this.gameCenterLink = dto.getGameCenterLink();
        this.gameType = dto.getGameType();
        this.gameState = dto.getGameState();
        this.startTimeUTC = dto.getStartTimeUTC();
        this.homeTeam = new Team(dto.getHomeTeam());
        this.awayTeam = new Team(dto.getAwayTeam());
        this.venue = new Venue(dto.getVenue());
        this.nhlGameDate = nhlDate; // 🔹 viktigt
    }
}