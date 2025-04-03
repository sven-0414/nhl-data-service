package se.sven.nhldataservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for transferring venue data from the NHL API.
 * Represents the arena where a game is played.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VenueDTO {
    private Long id;
    private String name;
    private String city;
    private String state;
    private String country;
    private String venueTimezone;
}