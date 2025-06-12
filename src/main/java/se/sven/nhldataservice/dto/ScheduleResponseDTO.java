package se.sven.nhldataservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class ScheduleResponseDTO {
    private String prevDate;
    private String currentDate;
    private String nextDate;
    private List<GameWeekDTO> gameWeek;
    private String preSeasonStartDate;
    private String regularSeasonStartDate;
    private String regularSeasonEndDate;
    private String playoffEndDate;
    private int numberOfGames;
}
