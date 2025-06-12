package se.sven.nhldataservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sven.nhldataservice.dto.GameDTO;

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
    private LocalDate nhlGameDate;
    private int homeScore;
    private int awayScore;
    private int period;
    private String gameCenterLink;

    // GameOutcomeDTO mappning:
    private String lastPeriodType;
    private String otPeriods;

    // ClockDTO mappning:
    private String timeRemaining;
    private Integer secondsRemaining;
    private Boolean clockRunning;
    private Boolean inIntermission;

    // PeriodDescriptorDTO mappning (utöver period):
    private String periodType;
    private Integer maxRegulationPeriods;

    // WinnerDTO mappning - winnerByPeriod:
    @ElementCollection
    @CollectionTable(name = "game_winner_periods", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "period_winner")
    private List<Integer> winnerByPeriodList;

    private Integer winnerByPeriodGameOutcome;

    // WinnerDTO mappning - winnerByGameOutcome:
    @ElementCollection
    @CollectionTable(name = "game_outcome_periods", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "period_winner")
    private List<Integer> winnerByGameOutcomePeriods;

    private Integer winnerByGameOutcomeResult;

    @ManyToOne(cascade = CascadeType.MERGE)  // Lägg till cascade
    @JoinColumn(name = "home_team_id")       // Explicit join column
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Team homeTeam;

    @ManyToOne(cascade = CascadeType.MERGE)  // Lägg till cascade
    @JoinColumn(name = "away_team_id")       // Explicit join column
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Team awayTeam;

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
        this.nhlGameDate = dto.getStartTimeUTC() != null ? dto.getStartTimeUTC().toLocalDate() : null;
        this.homeScore = dto.getHomeTeam() != null ? dto.getHomeTeam().getScore() : 0;
        this.awayScore = dto.getAwayTeam() != null ? dto.getAwayTeam().getScore() : 0;
        this.period = dto.getPeriodDescriptor() != null ? dto.getPeriodDescriptor().getNumber() : 0;
        this.gameCenterLink = dto.getGameCenterLink();

        // Mappa PeriodDescriptor data:
        if (dto.getPeriodDescriptor() != null) {
            this.periodType = dto.getPeriodDescriptor().getPeriodType();
            this.maxRegulationPeriods = dto.getPeriodDescriptor().getMaxRegulationPeriods();
        }

        // Mappa GameOutcome data:
        if (dto.getGameOutcome() != null) {
            this.lastPeriodType = dto.getGameOutcome().getLastPeriodType();
            this.otPeriods = dto.getGameOutcome().getOtPeriods();
        }

        // Mappa Clock data (för live matcher):
        if (dto.getClock() != null) {
            this.timeRemaining = dto.getClock().getTimeRemaining();
            this.secondsRemaining = dto.getClock().getSecondsRemaining();
            this.clockRunning = dto.getClock().getRunning();
            this.inIntermission = dto.getClock().getInIntermission();
        }

        // Mappa WinnerDTO - winnerByPeriod:
        if (dto.getWinnerByPeriod() != null) {
            this.winnerByPeriodList = dto.getWinnerByPeriod().getPeriods();
            this.winnerByPeriodGameOutcome = dto.getWinnerByPeriod().getGameOutcome();
        }

        // Mappa WinnerDTO - winnerByGameOutcome:
        if (dto.getWinnerByGameOutcome() != null) {
            this.winnerByGameOutcomePeriods = dto.getWinnerByGameOutcome().getPeriods();
            this.winnerByGameOutcomeResult = dto.getWinnerByGameOutcome().getGameOutcome();
        }

        // Säker mappning av teams:
        if (dto.getHomeTeam() != null) {
            this.homeTeam = new Team(dto.getHomeTeam());
        }
        if (dto.getAwayTeam() != null) {
            this.awayTeam = new Team(dto.getAwayTeam());
        }
    }
}