package se.sven.nhldataservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamDTO {
    private Long id;
    private String abbrev;
    private String logo;
    private LocalizedNameDTO placeName;
    private LocalizedNameDTO name;
    private int score;
}