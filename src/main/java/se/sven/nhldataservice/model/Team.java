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
public class Team {
    @Id
    private Long id;
    private String abbrev;
    private String name;
    private String city;
}