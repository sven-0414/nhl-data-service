package se.sven.nhldataservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sven.nhldataservice.dto.GameDTO;

/**
 * Game entity representing an NHL game with teams, scores, and live game data.
 * Maps NHL API data to database structure with denormalized fields for performance.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class Game {
    @Id
    private long id;
    private int season;
    private int gameType;
    private String gameDate;
    private String venue;
    private String neutralSite;
    private ZonedDateTime startTimeUTC;
    private String easternUTCOffset;
    private String venueUTCOffset;
    private String venueTimezone;
    private String gameState;
    private String gameScheduleState;
    private int homeScore;
    private int awayScore;
    private int period;
    private String gameCenterLink;
    private String otPeriods;

    // Clock data for live games (not fully testable during off-season)
    private String timeRemaining;
    private Integer secondsRemaining;
    private Boolean clockRunning;
    private Boolean inIntermission;
    private String periodType;
    private Integer maxRegulationPeriods;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "home_team_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Team homeTeam;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "away_team_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Team awayTeam;

    /**
     * Maps from NHL API's nested DTO structure, handling null values and extracting
     * data from LocalizedNameDTO objects.
     *
     * @param dto NHL API game data
     */
    public Game(GameDTO dto) {
        this.id = dto.getId();
        this.season = dto.getSeason();
        this.gameType = dto.getGameType();
        this.gameDate = dto.getGameDate();
        this.venue = dto.getVenue() != null ? dto.getVenue().getDefaultValue() : null;
        this.neutralSite = dto.getNeutralSite();
        this.startTimeUTC = dto.getStartTimeUTC();
        this.easternUTCOffset = dto.getEasternUTCOffset();
        this.venueUTCOffset = dto.getVenueUTCOffset();
        this.venueTimezone = dto.getVenueTimezone();
        this.gameState = dto.getGameState();
        this.gameScheduleState = dto.getGameScheduleState();
        this.homeScore = dto.getHomeTeam() != null ? dto.getHomeTeam().getScore() : 0;
        this.awayScore = dto.getAwayTeam() != null ? dto.getAwayTeam().getScore() : 0;
        this.period = dto.getPeriodDescriptor() != null ? dto.getPeriodDescriptor().getNumber() : 0;
        this.gameCenterLink = dto.getGameCenterLink();

        if (dto.getPeriodDescriptor() != null) {
            this.periodType = dto.getPeriodDescriptor().getPeriodType();
            this.maxRegulationPeriods = dto.getPeriodDescriptor().getMaxRegulationPeriods();
        }

        if (dto.getGameOutcome() != null) {
            this.otPeriods = dto.getGameOutcome().getOtPeriods();
        }

        if (dto.getClock() != null) {
            this.timeRemaining = dto.getClock().getTimeRemaining();
            this.secondsRemaining = dto.getClock().getSecondsRemaining();
            this.clockRunning = dto.getClock().getRunning();
            this.inIntermission = dto.getClock().getInIntermission();
        }

        if (dto.getHomeTeam() != null) {
            this.homeTeam = new Team(dto.getHomeTeam());
        }
        if (dto.getAwayTeam() != null) {
            this.awayTeam = new Team(dto.getAwayTeam());
        }
    }
}