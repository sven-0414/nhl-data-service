package se.sven.nhldataservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("commonName")
    private LocalizedNameDTO name;
    private int score;
}