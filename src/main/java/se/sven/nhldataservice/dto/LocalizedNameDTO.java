package se.sven.nhldataservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LocalizedNameDTO {
    @JsonProperty("default")
    private String defaultValue;
}