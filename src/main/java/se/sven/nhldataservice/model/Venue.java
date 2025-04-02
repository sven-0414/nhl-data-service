package se.sven.nhldataservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
