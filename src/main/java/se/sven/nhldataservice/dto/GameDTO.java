package se.sven.nhldataservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameDTO {
    private long id;
    private int season;
    private int gameType;
    private String gameDate;
    private LocalizedNameDTO venue;
    private String neutralSite;
    private ZonedDateTime startTimeUTC;
    private String easternUTCOffset;
    private String venueUTCOffset;
    private String venueTimezone;
    private String gameState;
    private String gameScheduleState;
    private TeamDTO awayTeam;
    private TeamDTO homeTeam;
    private PeriodDescriptorDTO periodDescriptor;
    private GameOutcomeDTO gameOutcome;
    private String gameCenterLink;
    private ClockDTO clock;
}