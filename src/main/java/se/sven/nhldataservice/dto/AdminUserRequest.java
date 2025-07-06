package se.sven.nhldataservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sven.nhldataservice.model.enums.RoleName;

import java.util.Set;

/**
 * DTO for admin operations on users.
 * Allows admins to modify user roles and enabled status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserRequest {

    private String username;
    private String email;
    private Boolean enabled;
    private Set<RoleName> roles;
}