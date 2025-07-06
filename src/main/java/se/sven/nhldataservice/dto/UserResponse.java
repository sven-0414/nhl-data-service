package se.sven.nhldataservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sven.nhldataservice.model.enums.RoleName;

import java.util.Set;

/**
 * DTO for user responses.
 * Excludes sensitive information like password.
 * Safe to send to clients.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private boolean enabled;
    private Set<RoleName> roles;
}