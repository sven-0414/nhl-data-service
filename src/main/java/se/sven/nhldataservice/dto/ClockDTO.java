package se.sven.nhldataservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ClockDTO {
    private String timeRemaining;
    private Integer secondsRemaining;
    private Boolean running;
    private Boolean inIntermission;
}