package se.sven.nhldataservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class GameWeekDTO {
    private String date;
    private String dayAbbrev;
    private int numberOfGames;
    private List<GameDTO> games;
}
