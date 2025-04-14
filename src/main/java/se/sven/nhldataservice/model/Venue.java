package se.sven.nhldataservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sven.nhldataservice.dto.VenueDTO;

/**
 * Represents a hockey venue or arena with details such as location and timezone.
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Venue {
    @Id
    private Long id;
    private String name;
    private String city;
    private String state;
    private String country;
    private String venueTimezone;

    /**
     * Skapar en ny Venue-entitet baserat på en DTO från NHL:s API.
     *
     * @param dto Data Transfer Object som innehåller arenainformation.
     */
    public Venue(VenueDTO dto) {
        this.id = dto.getId();
        this.name = dto.getName();
        this.city = dto.getCity();
        this.state = dto.getState();
        this.country = dto.getCountry();
        this.venueTimezone = dto.getVenueTimezone();
    }
}
