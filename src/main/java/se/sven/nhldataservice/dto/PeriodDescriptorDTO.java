package se.sven.nhldataservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PeriodDescriptorDTO {
    private int number;
    private String periodType;
    private Integer maxRegulationPeriods;
}