package se.sven.nhldataservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    private Character gameType;
    private Character gameState;
    private int period;
    private ZonedDateTime startTimeUTC;
    private LocalDate nhlGameDate;
    private int homeScore;
    private int awayScore;
    @ManyToOne
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Team homeTeam;
    @ManyToOne
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Team awayTeam;
    @ManyToOne
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Venue venue;

    /**
     * Skapar en ny Game-entitet baserat på en DTO från NHL:s API.
     * Konverterar även underliggande TeamDTO och VenueDTO till entiteter.
     *
     * @param dto Data Transfer Object som innehåller matchinformation.
     */

    public Game(GameDTO dto, ZonedDateTime startTimeUTC) {
        this.id = dto.getId();
        this.season = dto.getSeason();
        this.homeTeam = new Team(dto.getHomeTeam());
        this.awayTeam = new Team(dto.getAwayTeam());
        this.venue = new Venue(dto.getVenue());
        this.nhlGameDate = startTimeUTC.toLocalDate(); // eller som du föredrar
        this.homeScore = dto.getHomeScore();
        this.awayScore = dto.getAwayScore();
        this.gameState = dto.getGameState();
    }
}
