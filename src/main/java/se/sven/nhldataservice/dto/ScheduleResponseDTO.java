package se.sven.nhldataservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object (DTO) representing a response containing a list of NHL games
 * for a specific date or schedule range.
  * This object is typically used as the response from an API endpoint that returns
 * all scheduled games, possibly filtered by date or other criteria.
 * </p>
  * @author [Sven Eriksson]
 */

@Data
@NoArgsConstructor
public class ScheduleResponseDTO {
    private List<GameDTO> games;
}
