package se.sven.nhldataservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GameOutcomeDTO {
    private String lastPeriodType;
    private String otPeriods;
}
